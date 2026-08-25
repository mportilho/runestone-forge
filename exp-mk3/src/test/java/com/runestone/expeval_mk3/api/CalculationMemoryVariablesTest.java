package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

class CalculationMemoryVariablesTest {

    @Test
    void allViewsPreserveTheirResultTypeAndEffectiveExternalValue() {
        ExpressionEnvironment numbers = ExpressionEnvironment.builder()
                .externalSymbol("amount", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression numeric = ExpressionEngine.defaultEngine().compileOrThrow("amount + 1", numbers);

        ComputationWithMemory<Object> result = numeric.asResult().computeWithMemory();
        ComputationWithMemory<BigDecimal> math = numeric.asMath().computeWithMemory(Map.of("amount", BigDecimal.TEN));

        assertThat(result.result()).isEqualTo(new BigDecimal("2"));
        assertThat(result.memory().variableValueAt(0)).isEqualTo(BigDecimal.ONE);
        assertThat(numeric.asResult().computeWithMemory(Map.of("amount", BigDecimal.TEN)).result())
                .isEqualTo(numeric.asResult().compute(Map.of("amount", BigDecimal.TEN)));
        assertThat(math.result()).isEqualTo(new BigDecimal("11"));
        assertThat(math.memory().variableValueAt(0)).isEqualTo(BigDecimal.TEN);
        assertThat(numeric.asMath().computeWithMemory().result()).isEqualTo(numeric.asMath().compute());

        ExpressionEnvironment booleans = ExpressionEnvironment.builder()
                .externalSymbol("enabled", true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression logical = ExpressionEngine.defaultEngine().compileOrThrow("enabled", booleans);
        ComputationWithMemory<Boolean> logicalResult = logical.asLogical().computeWithMemory(Map.of("enabled", false));
        assertThat(logicalResult.result()).isFalse();
        assertThat(logicalResult.memory().variables()).containsExactly(
                new VariableEntry(new VariableKey("enabled", VariableOrigin.EXTERNAL), false));
        assertThat(logical.asLogical().computeWithMemory().result()).isEqualTo(logical.asLogical().compute());

        CompiledExpression assignments = ExpressionEngine.defaultEngine().compileOrThrow("total := amount + 1;", numbers);
        ComputationWithMemory<Map<String, Object>> assignmentResult =
                assignments.asAssignments().computeWithMemory(Map.of("amount", BigDecimal.TEN));
        assertThat(assignmentResult.result()).isEqualTo(Map.of("total", new BigDecimal("11")));
        assertThat(assignmentResult.memory().variables()).containsExactly(
                new VariableEntry(new VariableKey("amount", VariableOrigin.EXTERNAL), BigDecimal.TEN),
                new VariableEntry(new VariableKey("total", VariableOrigin.INTERNAL), new BigDecimal("11")));
        assertThat(assignments.asAssignments().computeWithMemory().result())
                .isEqualTo(assignments.asAssignments().compute());
    }

    @Test
    void publishesUsedExternalsBeforeFinalInternalValuesAndPreservesShadowing() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("unused", new BigDecimal("99"), ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("x", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("x := x + 1; x := x + 1; x", environment);

        CalculationMemory memory = expression.asMath().computeWithMemory(Map.of("x", BigDecimal.TEN)).memory();

        assertThat(memory.variables()).containsExactly(
                new VariableEntry(new VariableKey("x", VariableOrigin.EXTERNAL), BigDecimal.TEN),
                new VariableEntry(new VariableKey("x", VariableOrigin.INTERNAL), new BigDecimal("12")));
        assertThat(memory.calculations()).isEmpty();
    }

    @Test
    void assignmentsExcludeExternalSymbolsUsedOnlyByTheSkippedResult() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("base", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("resultOnly", BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "total := base + 1; total + resultOnly", environment);

        CalculationMemory assignmentMemory = expression.asAssignments().computeWithMemory().memory();
        CalculationMemory resultMemory = expression.asMath().computeWithMemory().memory();

        assertThat(assignmentMemory.variables()).extracting(entry -> entry.key().name())
                .containsExactly("base", "total");
        assertThat(resultMemory.variables()).extracting(entry -> entry.key().name())
                .containsExactly("base", "resultOnly", "total");
        assertThat(assignmentMemory.variableKeyAt(0)).isSameAs(resultMemory.variableKeyAt(0));
        assertThat(assignmentMemory.variableKeyAt(1)).isSameAs(resultMemory.variableKeyAt(2));
    }

    @Test
    void includesFixedExternalReadsRemovedByConstantFolding() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("fixed", new BigDecimal("4"), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("fixed + 1", environment);

        CalculationMemory memory = expression.asMath().computeWithMemory().memory();

        assertThat(memory.variables()).containsExactly(
                new VariableEntry(new VariableKey("fixed", VariableOrigin.EXTERNAL), new BigDecimal("4")));
    }

    @Test
    void explicitVariableSlotsExcludeCurrentItemsAndMemoSlots() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("rate", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("bonus", BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("amount", new BigDecimal("2"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "items := [1, 2]; mapped := items.map(@ -> @ + rate); "
                        + "base := amount + 1; mapped.sum() + (amount + 1) + (amount + 1) + bonus",
                environment);

        CalculationMemory assignmentMemory = expression.asAssignments().computeWithMemory().memory();
        CalculationMemory fullMemory = expression.asMath().computeWithMemory().memory();

        assertThat(assignmentMemory.variables()).extracting(entry -> entry.key().name())
                .containsExactly("rate", "amount", "items", "mapped", "base");
        assertThat(fullMemory.variables()).extracting(entry -> entry.key().name())
                .containsExactly("rate", "amount", "bonus", "items", "mapped", "base");
    }

    @Test
    void indexedAccessUsesSharedKeysAndListProjectionIsImmutableAndStateless() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("value", BigDecimal.ONE, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("value", environment);
        CalculationMemory first = expression.asMath().computeWithMemory().memory();
        CalculationMemory second = expression.asMath().computeWithMemory().memory();

        assertThat(first.variableCount()).isOne();
        assertThat(first.variableKeyAt(0)).isSameAs(second.variableKeyAt(0));
        assertThat(first.variables()).isNotSameAs(first.variables());
        assertThat(first.variables().get(0)).isNotSameAs(first.variables().get(0));
        assertThatThrownBy(() -> first.variables().add(first.variables().get(0)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> first.variableKeyAt(-1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> first.variableValueAt(1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> first.calculationKeyAt(0)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> first.calculationValueAt(0)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void memoryWithoutParticipantsReusesTheSharedEmptyInstance() {
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("1 + 2", ExpressionEnvironment.standard());

        CalculationMemory first = expression.asMath().computeWithMemory().memory();
        CalculationMemory second = expression.asResult().computeWithMemory().memory();

        assertThat(first).isSameAs(CalculationMemory.empty()).isSameAs(second);
        assertThat(first.variables()).isSameAs(List.of());
        assertThat(first.calculations()).isSameAs(List.of());
    }

    @Test
    void executionAndPublicMaterializationFailuresMatchCompute() {
        ResultExpression executionFailure = ExpressionEngine.defaultEngine()
                .compileOrThrow("1 / 0", ExpressionEnvironment.standard())
                .asResult();

        ExpressionExecutionException normalExecutionFailure =
                catchThrowableOfType(executionFailure::compute, ExpressionExecutionException.class);
        ExpressionExecutionException memoryExecutionFailure =
                catchThrowableOfType(executionFailure::computeWithMemory, ExpressionExecutionException.class);
        assertThat(memoryExecutionFailure.diagnostic()).isEqualTo(normalExecutionFailure.diagnostic());

        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new AssignmentsExpressionTest.OversizedCollectionFunctions(), FunctionPurity.IMPURE)
                .maxMaterializedSize(2)
                .build();
        ResultExpression materializationFailure = ExpressionEngine.defaultEngine()
                .compileOrThrow("makeOversizedList()", environment)
                .asResult();

        ExpressionExecutionException normalMaterializationFailure =
                catchThrowableOfType(materializationFailure::compute, ExpressionExecutionException.class);
        ExpressionExecutionException memoryMaterializationFailure =
                catchThrowableOfType(materializationFailure::computeWithMemory, ExpressionExecutionException.class);
        assertThat(memoryMaterializationFailure.diagnostic()).isEqualTo(normalMaterializationFailure.diagnostic());

        AssignmentsExpression assignmentMaterializationFailure = ExpressionEngine.defaultEngine()
                .compileOrThrow("items := makeOversizedList();", environment)
                .asAssignments();
        ExpressionExecutionException normalAssignmentFailure = catchThrowableOfType(
                assignmentMaterializationFailure::compute, ExpressionExecutionException.class);
        ExpressionExecutionException memoryAssignmentFailure = catchThrowableOfType(
                assignmentMaterializationFailure::computeWithMemory, ExpressionExecutionException.class);
        assertThat(memoryAssignmentFailure.diagnostic()).isEqualTo(normalAssignmentFailure.diagnostic());
    }
}
