package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ObjectType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.ConstantExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.FrameReadExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.RegisteredMethodExecutableNode;
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
 * Proves the two Etapa 7 constant sources added by issue #117: a pure navigation prefix over a
 * constant receiver, and a non-overridable External Symbol read, whose effective value is its
 * validated environment default. Also proves the Leitura Dobrada bookkeeping and the rule that
 * folding a symbol's only read never authorizes dropping its declaration or its frame slot.
 */
class NavigationPrefixAndExternalSymbolFoldingTest {

    private static final ObjectType ACCOUNT_TYPE = new ObjectType(Account.class.getName());

    @Test
    void foldsAPureNavigationPrefixOverAConstantReceiver() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("account", ACCOUNT_TYPE, new Account("Ana", BigDecimal.TEN),
                        ExternalSymbolOverwritePolicy.FIXED)
                .registerJavaType(Account.class)
                .build();
        SemanticModel model = resolve("account.name", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo("Ana");
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
    }

    @Test
    void doesNotFoldANavigationPrefixWithAnImpureLink() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("account", ACCOUNT_TYPE, new Account("Ana", BigDecimal.TEN),
                        ExternalSymbolOverwritePolicy.FIXED)
                .registerJavaType(Account.class)
                .registerJavaTypeMethod(Account.class, "reciprocal", FunctionPurity.IMPURE)
                .build();
        SemanticModel model = resolve("account.reciprocal()", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(RegisteredMethodExecutableNode.class);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
    }

    @Test
    void abandonsTheFoldOfAPureLinkThatFailsAtFoldTimeAndFailsAtExecutionWithTheOracleDiagnostic() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("account", ACCOUNT_TYPE, new Account("Ana", BigDecimal.ZERO),
                        ExternalSymbolOverwritePolicy.FIXED)
                .registerJavaType(Account.class)
                .registerJavaTypeMethod(Account.class, "reciprocal", FunctionPurity.PURE)
                .build();
        SemanticModel model = resolve("account.reciprocal()", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(RegisteredMethodExecutableNode.class);
        assertThatThrownBy(() -> plan.compute(Map.of(), Clock.systemUTC()))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> assertThat(((ExpressionExecutionException) exception).diagnostic().code())
                        .isEqualTo(DiagnosticCode.RUNTIME_MEMBER_ACCESS_FAILURE.name()));
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
    }

    @Test
    void foldsANonOverridableExternalSymbolReadToItsValidatedDefault() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("rate", ScalarType.NUMBER, BigDecimal.valueOf(12), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("rate * 12", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("144"));
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
    }

    @Test
    void doesNotFoldAnOverridableExternalSymbolReadAndItsOverrideStillTakesEffect() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("rate", ScalarType.NUMBER, BigDecimal.valueOf(12), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("rate", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(FrameReadExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("12"));
        assertThat((BigDecimal) plan.compute(Map.of("rate", BigDecimal.valueOf(99)), Clock.systemUTC()))
                .isEqualByComparingTo(new BigDecimal("99"));
    }

    @Test
    void rejectsOverrideOfANonOverridableSymbolWithTheSameDiagnosticAfterItsOnlyReadFolds() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("rate", ScalarType.NUMBER, BigDecimal.valueOf(12), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("rate", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        // The message distinguishes "found but not overridable" from "unknown external symbol": had
        // folding the symbol's only read dropped its declaration or frame slot, the override would
        // instead fail as unknown, not as non-overridable, with a different message under the same code.
        assertThatThrownBy(() -> plan.compute(Map.of("rate", BigDecimal.valueOf(99)), Clock.systemUTC()))
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(exception -> {
                    var diagnostic = ((ExpressionExecutionException) exception).diagnostic();
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.RUNTIME_INVALID_EXTERNAL_INPUT.name());
                    assertThat(diagnostic.message()).contains("rate").contains("not overridable");
                });
    }

    @Test
    void recordsTheFoldedReadWithSymbolNameNodeIdentitySourceSpanAndValue() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("rate", ScalarType.NUMBER, BigDecimal.valueOf(12), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("rate", environment);
        IdentifierNode identifier = (IdentifierNode) model.ast().resultExpression().orElseThrow();

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.foldedVariableReads()).singleElement().satisfies(read -> {
            assertThat(read.symbolName()).isEqualTo("rate");
            assertThat(read.nodeId()).isEqualTo(identifier.id());
            assertThat(read.sourceSpan()).isEqualTo(identifier.sourceSpan());
            assertThat((BigDecimal) read.foldedValue()).isEqualByComparingTo(new BigDecimal("12"));
        });
    }

    @Test
    void doesNotRecordAFoldedReadForAnOverridableSymbol() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("rate", ScalarType.NUMBER, BigDecimal.valueOf(12), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("rate", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.foldedVariableReads()).isEmpty();
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

    public record Account(String name, BigDecimal score) {

        public BigDecimal reciprocal() {
            return BigDecimal.ONE.divide(score);
        }
    }
}
