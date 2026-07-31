package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.PostfixExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.DeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.DestructuringMinimumSizeDeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.FactorialIntegralDeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.FactorialMaxBoundDeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.FactorialNonNegativeDeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.PowerRealDomainDeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the Phase 5 planner core from issue #94: assignment-only files plan without a synthetic
 * result, assignments run in source order, deferred checks reach the node that needs them, incomplete
 * successful-model metadata fails as an internal invariant, and the produced collections are immutable.
 */
class ExecutionPlanBuilderTest {

    @Test
    void assignmentOnlyFileProducesAValidPlanWithoutInventingAResult() {
        SemanticModel model = resolve("a := 1; b := a + 1;", numberEnvironment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, numberEnvironment());

        assertThat(plan.hasResult()).isFalse();
        assertThat(plan.compute(Map.of())).isNull();
    }

    @Test
    void assignmentsExecuteInSourceOrderSoLaterAssignmentsCanReadEarlierOnes() {
        SemanticModel model = resolve("a := 1; b := a + 1; c := b + 1; c", numberEnvironment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, numberEnvironment());

        assertThat(plan.hasResult()).isTrue();
        assertThat(plan.compute(Map.of())).isEqualTo(new BigDecimal("3"));
    }

    @Test
    void planCollectionsAreImmutable() throws Exception {
        SemanticModel model = resolve("a := 1;", numberEnvironment());
        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, numberEnvironment());

        assertThatThrownBy(() -> plan.assignments().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void attachesTheFactorialDeferredChecksToThePostfixNodeForANonConstantOperand() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("x!", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        List<DeferredCheck> checks = plan.resultExpression().deferredChecks();
        assertThat(plan.resultExpression()).isInstanceOf(PostfixExecutableNode.class);
        assertThat(checks).hasAtLeastOneElementOfType(FactorialIntegralDeferredCheck.class);
        assertThat(checks).hasAtLeastOneElementOfType(FactorialNonNegativeDeferredCheck.class);
        assertThat(checks).hasAtLeastOneElementOfType(FactorialMaxBoundDeferredCheck.class);
    }

    @Test
    void attachesThePowerRealDomainDeferredCheckToTheExponentiationNodeForANonConstantBase() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.valueOf(-2), ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("y", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("x ^ y", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression().deferredChecks())
                .hasAtLeastOneElementOfType(PowerRealDomainDeferredCheck.class);
    }

    @Test
    void attachesTheDestructuringMinimumSizeDeferredCheckToTheAssignmentForAnUnknownShapeSource() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items", new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, BigDecimal.TWO), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("[a, b] := items;", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.assignments()).singleElement()
                .extracting(AssignmentExecutable::deferredChecks, InstanceOfAssertFactories.list(DeferredCheck.class))
                .hasAtLeastOneElementOfType(DestructuringMinimumSizeDeferredCheck.class);
    }

    @Test
    void rejectsAModelMissingASymbolBindingAsAnInternalInvariantViolation() throws Exception {
        ExpressionEnvironment environment = numberEnvironment();
        SemanticModel model = resolve("a := 1; a", environment);
        clearSymbolBindings(model);

        assertThatThrownBy(() -> new ExecutionPlanBuilder().build(model, environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("symbol binding");
    }

    @Test
    void rejectsAModelMissingAPreparedLiteralValueAsAnInternalInvariantViolation() throws Exception {
        ExpressionEnvironment environment = numberEnvironment();
        SemanticModel model = resolve("1", environment);
        clearPreparedValues(model);

        assertThatThrownBy(() -> new ExecutionPlanBuilder().build(model, environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("prepared literal value");
    }

    private static void clearSymbolBindings(SemanticModel model) throws Exception {
        Field field = SemanticModel.class.getDeclaredField("symbolBindings");
        field.setAccessible(true);
        field.set(model, Map.of());
    }

    private static void clearPreparedValues(SemanticModel model) throws Exception {
        Field field = SemanticModel.class.getDeclaredField("preparedValues");
        field.setAccessible(true);
        field.set(model, Map.of());
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

    private static ExpressionEnvironment numberEnvironment() {
        return ExpressionEnvironment.builder().build();
    }
}
