package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.ConstantExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.FrameReadExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.FunctionCallExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the issue #118 elisions: a scalar assertion over a value whose resolved type already
 * matches the asserted type is a proven no-op and disappears from the plan, and {@code not not x}
 * cancels structurally. Both are gated on {@code folding} the same way as every other Etapa 7
 * source, so the Unoptimized Oracle never performs them; {@link PlanEquivalenceHarness} is the
 * primary proof that neither elision changes observable behavior.
 */
class AssertionElisionAndDoubleNegationFoldingTest {

    @Test
    void elidesAsNumberOverAConstantOfTheExactType() {
        SemanticModel model = resolve("asNumber(5)", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("5"));
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void elidesAsTextOverAConstantOfTheExactType() {
        SemanticModel model = resolve("asText(\"hi\")", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo("hi");
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void elidesAsBoolOverAConstantOfTheExactType() {
        SemanticModel model = resolve("asBool(true)", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void elidesAsDateAsTimeAndAsDateTimeOverConstantsOfTheExactType() {
        SemanticModel model = resolve("asDate(d\"2026-07-09\") = d\"2026-07-09\" and asTime(t\"10:15:30\") = t\"10:15:30\""
                + " and asDateTime(dt\"2026-07-09T10:15:30\") = dt\"2026-07-09T10:15:30\"", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void elidesAScalarAssertionOverANonConstantValueOfTheExactType() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("asNumber(x)", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(FrameReadExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of("x", BigDecimal.TEN), Clock.systemUTC()))
                .isEqualByComparingTo(BigDecimal.TEN);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of("x", BigDecimal.TEN), Clock.systemUTC());
    }

    @Test
    void doesNotElideAScalarAssertionOverADifferentTypeAndStillConverts() {
        SemanticModel model = resolve("asNumber(\"12.50\")", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("12.50"));
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void doesNotElideAScalarAssertionOverANonConstantDifferentType() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("s", ScalarType.STRING, "12.50", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("asNumber(s)", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(FunctionCallExecutableNode.class);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
    }

    @Test
    void leavesAFailingDifferentTypeConversionUnelidedAndFailsWithTheOracleDiagnostic() {
        SemanticModel model = resolve("asNumber(\"not-a-number\")", environment());

        ExecutionPlan optimized = new ExecutionPlanBuilder().build(model, environment());

        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
        assertThatThrownBy(() -> optimized.compute(Map.of(), Clock.systemUTC()))
                .isInstanceOf(ExpressionExecutionException.class);
    }

    @Test
    void reducesNotNotToItsOperand() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("ativo", ScalarType.BOOLEAN, true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("!!ativo", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(FrameReadExecutableNode.class);
        assertThat(plan.compute(Map.of("ativo", true), Clock.systemUTC())).isEqualTo(true);
        assertThat(plan.compute(Map.of("ativo", false), Clock.systemUTC())).isEqualTo(false);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of("ativo", true), Clock.systemUTC());
    }

    @Test
    void reducesAnOddChainOfNotToASingleNegation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("ativo", ScalarType.BOOLEAN, true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("!!!ativo", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.compute(Map.of("ativo", true), Clock.systemUTC())).isEqualTo(false);
        assertThat(plan.compute(Map.of("ativo", false), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of("ativo", true), Clock.systemUTC());
    }

    @Test
    void reducesALongerEvenChainOfNotToItsOperand() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("ativo", ScalarType.BOOLEAN, true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("!!!!ativo", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(FrameReadExecutableNode.class);
        assertThat(plan.compute(Map.of("ativo", true), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of("ativo", true), Clock.systemUTC());
    }

    private static ExpressionEnvironment environment() {
        return ExpressionEnvironment.builder().build();
    }

    private static SemanticModel resolve(String source, ExpressionEnvironment environment) {
        ExpressionFileNode ast = ast(source);
        SemanticResolutionSuccess result = (SemanticResolutionSuccess) new SemanticResolver().resolve(ast, environment);
        return result.model();
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }
}
