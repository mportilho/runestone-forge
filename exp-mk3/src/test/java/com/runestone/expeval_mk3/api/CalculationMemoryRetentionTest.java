package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.memory.CalculationMemorySchema;
import com.runestone.expeval_mk3.internal.memory.CalculationRecorder;
import com.runestone.expeval_mk3.internal.memory.DefaultCalculationMemory;
import com.runestone.expeval_mk3.internal.memory.VariableMemorySchema;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlan;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.support.DeterministicObjectGraph;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CalculationMemoryRetentionTest {

    @Test
    void retainedMemoryContainsOnlyExactPayloadAndStandalonePublicMetadata() {
        RetentionFixture fixture = memoryFixture();
        CalculationMemory memory = fixture.memory();
        DeterministicObjectGraph graph = DeterministicObjectGraph.from(memory);

        assertThat(graph.containsIdentity(fixture.capturedValue())).isTrue();
        assertThat(graph.containsIdentity(fixture.engine())).isFalse();
        assertThat(graph.containsIdentity(fixture.compiled())).isFalse();
        assertThat(graph.containsIdentity(fixture.plan())).isFalse();
        assertThat(graph.containsIdentity(fixture.environment())).isFalse();
        assertThat(graph.containsIdentity(fixture.provider())).isFalse();
        assertThat(graph.pathToType(ExecutionPlan.class)).isEmpty();
        assertThat(graph.pathToType(ExecutableNode.class)).isEmpty();
        assertThat(graph.pathToType(ExecutionScope.class)).isEmpty();
        assertThat(graph.pathToType(CalculationRecorder.class)).isEmpty();
        assertThat(graph.pathToType(SemanticModel.class)).isEmpty();
        assertThat(graph.pathToType(ExpressionEnvironment.class)).isEmpty();
        assertThat(graph.pathToType(ParserRuleContext.class)).isEmpty();
        assertThat(graph.objects())
                .noneMatch(value -> value.getClass().getPackageName().equals("com.runestone.expeval_mk3.internal.ast")
                        && !value.getClass().isEnum())
                .noneMatch(value -> value instanceof String text && text.equals(fixture.source()));

        assertExactColumns(memory);
    }

    @Test
    void retainedPlanCannotReachMemoriesOrValuesFromCompletedExecutions() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("value", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression compiled = ExpressionEngine.builder().build()
                .compileOrThrow("(value + 1) * (value + 1)", environment);
        BigDecimal firstValue = new BigDecimal("314159265358979323846");
        BigDecimal secondValue = new BigDecimal("271828182845904523536");
        CalculationMemory firstMemory = compiled.asMath().computeWithMemory(Map.of("value", firstValue)).memory();
        CalculationMemory secondMemory = compiled.asMath().computeWithMemory(Map.of("value", secondValue)).memory();

        DeterministicObjectGraph graph = DeterministicObjectGraph.from(planOf(compiled));

        assertThat(graph.pathToIdentity(firstMemory)).isEmpty();
        assertThat(graph.pathToIdentity(secondMemory)).isEmpty();
        assertThat(graph.pathToIdentity(firstValue)).isEmpty();
        assertThat(graph.pathToIdentity(secondValue)).isEmpty();
        assertThat(graph.pathToType(ExecutionScope.class)).isEmpty();
        assertThat(graph.pathToType(CalculationRecorder.class)).isEmpty();
        assertThat(graph.pathToType(DefaultCalculationMemory.class)).isEmpty();
    }

    @Test
    void finalColumnsAreExactForEmptyDensePrefixAndGappedMemory() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new RetentionFunctions(), FunctionPurity.IMPURE)
                .build();
        ExpressionEngine engine = ExpressionEngine.builder().build();

        CalculationMemory empty = engine.compileOrThrow("1 + 2", environment).asMath().computeWithMemory().memory();
        CalculationMemory dense = engine.compileOrThrow("mark(1) + mark(2) + mark(3)", environment)
                .asMath().computeWithMemory().memory();
        CalculationMemory prefix = engine.compileOrThrow("markBoolean(false) and markBoolean(true)", environment)
                .asLogical().computeWithMemory().memory();
        CalculationMemory gapped = engine.compileOrThrow(
                        "if markBoolean(false) then mark(1) else mark(2) endif", environment)
                .asMath().computeWithMemory().memory();

        assertExactColumns(empty);
        assertExactColumns(dense);
        assertExactColumns(prefix);
        assertExactColumns(gapped);
        assertThat(fieldValue(dense, "calculationOrdinals", int[].class)).isNull();
        assertThat(fieldValue(prefix, "calculationOrdinals", int[].class)).isNull();
        assertThat(fieldValue(gapped, "calculationOrdinals", int[].class)).hasSize(gapped.calculationCount());
    }

    @Test
    void listProjectionsAreReachableOnlyWhenTheCallerRetainsThem() {
        CalculationMemory memory = memoryFixture().memory();
        List<VariableEntry> variables = memory.variables();
        List<CalculationEntry> calculations = memory.calculations();

        DeterministicObjectGraph memoryGraph = DeterministicObjectGraph.from(memory);
        assertThat(memoryGraph.pathToIdentity(variables)).isEmpty();
        assertThat(memoryGraph.pathToIdentity(calculations)).isEmpty();
        assertThat(DeterministicObjectGraph.from(memory, variables).pathToIdentity(variables)).isPresent();
        assertThat(DeterministicObjectGraph.from(memory, calculations).pathToIdentity(calculations)).isPresent();
        assertThat(memory.variables()).isNotSameAs(variables);
        assertThat(memory.calculations()).isNotSameAs(calculations);
    }

    @Test
    void schemasAreTopLevelFinalObjectsWithoutSyntheticOuterReferences() {
        for (Class<?> schema : List.of(CalculationMemorySchema.class, VariableMemorySchema.class)) {
            assertThat(schema.getEnclosingClass()).isNull();
            assertThat(Modifier.isFinal(schema.getModifiers())).isTrue();
            assertThat(schema.getDeclaredFields()).noneMatch(Field::isSynthetic);
            assertThat(schema.getDeclaredFields()).noneMatch(field ->
                    ExecutionPlan.class.isAssignableFrom(field.getType())
                            || ExecutableNode.class.isAssignableFrom(field.getType())
                            || ExpressionEnvironment.class.isAssignableFrom(field.getType())
                            || SemanticModel.class.isAssignableFrom(field.getType()));
        }
    }

    private static RetentionFixture memoryFixture() {
        RetentionFunctions provider = new RetentionFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("payload", BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(provider, FunctionPurity.IMPURE)
                .build();
        String source = "memo := (payload + 1) * (payload + 1); mark(memo)";
        ExpressionEngine engine = ExpressionEngine.builder().build();
        CompiledExpression compiled = engine.compileOrThrow(source, environment);
        BigDecimal inputValue = new BigDecimal("12345678901234567890");
        CalculationMemory memory = compiled.asMath()
                .computeWithMemory(Map.of("payload", inputValue))
                .memory();
        BigDecimal capturedValue = (BigDecimal) memory.variableValueAt(0);
        return new RetentionFixture(
                memory, capturedValue, source, engine, compiled, planOf(compiled), environment, provider);
    }

    private static void assertExactColumns(CalculationMemory memory) {
        Object[] variableValues = fieldValue(memory, "variableValues", Object[].class);
        Object[] calculationValues = fieldValue(memory, "calculationValues", Object[].class);
        int[] ordinals = fieldValue(memory, "calculationOrdinals", int[].class);

        assertThat(variableValues).hasSize(memory.variableCount());
        assertThat(calculationValues).hasSize(memory.calculationCount());
        if (ordinals != null) {
            assertThat(ordinals).hasSize(memory.calculationCount());
        }
        DeterministicObjectGraph graph = DeterministicObjectGraph.from(memory);
        assertThat(graph.pathToType(ExecutionScope.class)).isEmpty();
        assertThat(graph.pathToType(CalculationRecorder.class)).isEmpty();
    }

    private static ExecutionPlan planOf(CompiledExpression compiled) {
        return fieldValue(compiled, "plan", ExecutionPlan.class);
    }

    private static <T> T fieldValue(Object target, String name, Class<T> type) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return type.cast(field.get(target));
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private record RetentionFixture(
            CalculationMemory memory,
            BigDecimal capturedValue,
            String source,
            ExpressionEngine engine,
            CompiledExpression compiled,
            ExecutionPlan plan,
            ExpressionEnvironment environment,
            RetentionFunctions provider) {
    }

    public static final class RetentionFunctions {
        public BigDecimal mark(BigDecimal value) {
            return value;
        }

        public boolean markBoolean(boolean value) {
            return value;
        }
    }
}
