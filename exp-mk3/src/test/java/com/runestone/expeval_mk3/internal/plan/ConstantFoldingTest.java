package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.BinaryExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.BetweenExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ConstantExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.PostfixExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.UnaryExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.PowerRealDomainDeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.RootRealDomainDeferredCheck;
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
 * Proves the issue #115 eager constant-folding core: binary, unary, postfix, regex with a constant
 * left operand, and {@code between} fold when every required child is constant, and a subtree that
 * fails while folding is left unfolded so it fails at execution exactly as the Unoptimized Oracle
 * would, with the deferred check that guards it intact. Eligibility, node substitution, and visit
 * counts are not asserted directly per the issue's testing decisions; only the resulting node's own
 * declared type (constant or not) and the plan's observable execution outcome are.
 */
class ConstantFoldingTest {

    @Test
    void foldsAConstantArithmeticExpression() {
        SemanticModel model = resolve("2 + 3 * 4", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("14"));
    }

    @Test
    void doesNotFoldAnArithmeticExpressionWithANonConstantOperand() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("x + 1", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isNotInstanceOf(ConstantExecutableNode.class);
    }

    @Test
    void foldsAConstantComparison() {
        SemanticModel model = resolve("10 > 3", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
    }

    @Test
    void foldsAConstantUnaryExpression() {
        SemanticModel model = resolve("-(2 + 3)", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("-5"));
    }

    @Test
    void doesNotFoldAUnaryExpressionWithANonConstantOperand() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("-x", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(UnaryExecutableNode.class);
    }

    @Test
    void foldsAConstantPostfixExpression() {
        SemanticModel model = resolve("5!", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("120"));
    }

    @Test
    void doesNotFoldAPostfixExpressionWithANonConstantOperand() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("x!", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(PostfixExecutableNode.class);
    }

    @Test
    void foldsARegexMatchWithAConstantLeftOperand() {
        SemanticModel model = resolve("\"ab\" =~ \"a.*\"", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
    }

    @Test
    void doesNotFoldARegexMatchWithANonConstantLeftOperand() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("s", ScalarType.STRING, "ab", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("s =~ \"a.*\"", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(BinaryExecutableNode.class);
    }

    @Test
    void foldsABetweenExpressionWithAllConstantParts() {
        SemanticModel model = resolve("3 between 1 and 7", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
    }

    @Test
    void doesNotFoldABetweenExpressionWithANonConstantBound() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("upper", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("3 between 1 and upper", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(BetweenExecutableNode.class);
    }

    @Test
    void leavesAConstantDivisionByZeroUnfoldedAndFailsAtExecutionWithTheOracleCode() {
        SemanticModel model = resolve("2 / 0", environment());

        ExecutionPlan optimized = new ExecutionPlanBuilder().build(model, environment());
        assertThat(optimized.resultExpression()).isInstanceOf(BinaryExecutableNode.class);

        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
        assertThatThrownBy(() -> optimized.compute(Map.of(), Clock.systemUTC()))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo(DiagnosticCode.RUNTIME_UNDEFINED_OPERATION.name()));
    }

    @Test
    void abandonsAFoldOnAPowerRealDomainViolationAndPreservesTheDeferredCheck() {
        ExpressionEnvironment environment = environment();
        SemanticModel model = resolve("(0 + 0) ^ (0 - 2)", environment);

        ExecutionPlan optimized = new ExecutionPlanBuilder().build(model, environment);

        assertThat(optimized.resultExpression()).isInstanceOf(BinaryExecutableNode.class);
        assertThat(optimized.resultExpression().deferredChecks())
                .hasAtLeastOneElementOfType(PowerRealDomainDeferredCheck.class);

        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
        assertThatThrownBy(() -> optimized.compute(Map.of(), Clock.systemUTC()))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo(DiagnosticCode.RUNTIME_POWER_UNDEFINED.name()));
    }

    @Test
    void abandonsAFoldOnARootRealDomainViolationAndPreservesTheDeferredCheck() {
        ExpressionEnvironment environment = environment();
        SemanticModel model = resolve("2 root (0 - 8)", environment);

        ExecutionPlan optimized = new ExecutionPlanBuilder().build(model, environment);

        assertThat(optimized.resultExpression()).isInstanceOf(BinaryExecutableNode.class);
        assertThat(optimized.resultExpression().deferredChecks())
                .hasAtLeastOneElementOfType(RootRealDomainDeferredCheck.class);

        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
        assertThatThrownBy(() -> optimized.compute(Map.of(), Clock.systemUTC()))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo(DiagnosticCode.RUNTIME_ROOT_COMPLEX_DOMAIN.name()));
    }

    @Test
    void abandonsAFoldOnAFactorialExceedingTheEnvironmentMaximumAndPreservesTheDeferredChecks() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().maxFactorialInput(15).build();
        SemanticModel model = resolve("(10 + 10)!", environment);

        ExecutionPlan optimized = new ExecutionPlanBuilder().build(model, environment);

        assertThat(optimized.resultExpression()).isInstanceOf(PostfixExecutableNode.class);
        assertThat(optimized.resultExpression().deferredChecks()).isNotEmpty();

        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
        assertThatThrownBy(() -> optimized.compute(Map.of(), Clock.systemUTC()))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo(DiagnosticCode.RUNTIME_FACTORIAL_EXCEEDS_MAXIMUM.name()));
    }

    @Test
    void discardsTheDeferredCheckOnlyWhenTheFoldSucceeds() {
        SemanticModel model = resolve("(1 + 1) ^ (0 - 2)", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.resultExpression().deferredChecks()).isEmpty();
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("0.25"));

        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
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
