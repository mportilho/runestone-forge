package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssignmentsExpressionTest {

    @Test
    void computesEveryInternalSymbolOnceInFirstCreationOrderWithReassignmentAndDestructuring() {
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "b := 1; a := 2; [c, d] := [3, 4]; b := 5;", ExpressionEnvironment.standard());

        Map<String, Object> result = expression.asAssignments().compute();

        assertThat(result.keySet()).containsExactly("b", "a", "c", "d");
        assertThat(result).containsEntry("b", new BigDecimal("5"));
        assertThat(result).containsEntry("a", new BigDecimal("2"));
        assertThat(result).containsEntry("c", new BigDecimal("3"));
        assertThat(result).containsEntry("d", new BigDecimal("4"));
    }

    @Test
    void destructuringEvaluatesItsSourceExactlyOnceRegardlessOfTargetCount() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "[a, b, c] := trackSource();", environment);

        Map<String, Object> result = expression.asAssignments().compute();

        assertThat(result.keySet()).containsExactly("a", "b", "c");
        assertThat(result).containsEntry("a", new BigDecimal("1"));
        assertThat(result).containsEntry("b", new BigDecimal("2"));
        assertThat(result).containsEntry("c", new BigDecimal("3"));
        assertThat(functions.invocations()).isOne();
    }

    @Test
    void nestedMapValuesStillUseCanonicalPublicMapOrderingUnderFirstCreationTopLevelOrder() {
        Map<String, BigDecimal> unordered = new LinkedHashMap<>();
        unordered.put("zeta", BigDecimal.ONE);
        unordered.put("alpha", BigDecimal.TEN);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("scores", new MapType(ScalarType.NUMBER), unordered, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("b := 1; nested := scores;", environment);

        Map<String, Object> result = expression.asAssignments().compute();

        assertThat(result.keySet()).containsExactly("b", "nested");
        assertThat(((Map<String, Object>) result.get("nested")).keySet()).containsExactly("alpha", "zeta");
    }

    @Test
    void skipsAnOptionalFinalExpressionWithoutEvaluatingOrExposingIt() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("a := 1; fail()", environment);

        Map<String, Object> result = expression.asAssignments().compute();

        assertThat(result).isEqualTo(Map.of("a", new BigDecimal("1")));
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void skippedFinalExpressionIsStillSemanticallyValidatedAtCompilation() {
        assertThatThrownBy(() -> ExpressionEngine.defaultEngine().compileOrThrow("a := 1; missing", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics()).anySatisfy(diagnostic ->
                                assertThat(diagnostic.code()).isEqualTo("SEMANTIC_UNKNOWN_SYMBOL")));
    }

    @Test
    void assignmentEffectsRunInSourceOrderRegardlessOfTheFinalExpression() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "a := trackOrder(1); b := trackOrder(2); a + b", environment);

        expression.asAssignments().compute();

        assertThat(functions.order()).containsExactly(1, 2);
    }

    @Test
    void supportsOverridesAffectingTheAssignmentsOutcome() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("base", new BigDecimal("4"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("total := base + 1;", environment);

        assertThat(expression.asAssignments().compute()).isEqualTo(Map.of("total", new BigDecimal("5")));
        assertThat(expression.asAssignments().compute(Map.of("base", new BigDecimal("10"))))
                .isEqualTo(Map.of("total", new BigDecimal("11")));
    }

    @Test
    void overridesAreValidatedBeforeAnyAssignmentEffectRuns() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("a := trackOrder(1);", environment);

        assertThatThrownBy(() -> expression.asAssignments().compute(Map.of("bogus", 1)))
                .isInstanceOf(ExpressionExecutionException.class);
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void internalSymbolShadowingAnExternalSymbolExposesTheInternalFinalValue() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("x := 1;", environment);

        assertThat(expression.asAssignments().compute()).isEqualTo(Map.of("x", new BigDecimal("1")));
    }

    @Test
    void assignmentOnlySourceWithNoFinalExpressionComputesThroughTheAssignmentsView() {
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("b := 1; a := 2;", ExpressionEnvironment.standard());

        assertThat(expression.asAssignments().compute()).isEqualTo(Map.of("a", new BigDecimal("2"), "b", new BigDecimal("1")));
    }

    @Test
    void rejectsAFileWithoutAnyAssignmentBeforeExecution() {
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("1 + 2", ExpressionEnvironment.standard());

        assertThatThrownBy(expression::asAssignments)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                        assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.NO_ASSIGNMENTS));
    }

    @Test
    void rejectsANonPubliclyExposableAssignedSymbolBeforeExecution() {
        WidgetFunctions functions = new WidgetFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .registerJavaType(Widget.class)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("w := markWidget();", environment);

        assertThatThrownBy(expression::asAssignments)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                        assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.TYPE_NOT_PUBLICLY_EXPOSABLE));
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void rejectsTooManyAssignmentEntriesBeforeExecution() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .maxMaterializedSize(1)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow(
                "a := trackOrder(1); b := trackOrder(2);", environment);

        assertThatThrownBy(expression::asAssignments)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                        assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.MATERIALIZATION_LIMIT_EXCEEDED));
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void enforcesTheNestedMaterializationLimitIndependentlyAtRuntime() {
        OversizedCollectionFunctions functions = new OversizedCollectionFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.PURE)
                .maxMaterializedSize(2)
                .build();
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("a := makeOversizedList();", environment);

        assertThatThrownBy(() -> expression.asAssignments().compute())
                .isInstanceOf(ExpressionExecutionException.class);
    }

    @Test
    void everyReturnedMapAndNestedContainerIsImmutable() {
        CompiledExpression expression = ExpressionEngine.defaultEngine().compileOrThrow("items := [1, 2];", ExpressionEnvironment.standard());

        Map<String, Object> result = expression.asAssignments().compute();

        assertThatThrownBy(() -> result.put("extra", BigDecimal.ONE)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> ((List<Object>) result.get("items")).add(BigDecimal.TEN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    public static final class CountingFunctions {

        private final AtomicInteger invocations = new AtomicInteger();
        private final List<Integer> order = new ArrayList<>();

        public BigDecimal fail() {
            invocations.incrementAndGet();
            throw new IllegalStateException("must not be invoked");
        }

        public BigDecimal trackOrder(BigDecimal value) {
            invocations.incrementAndGet();
            order.add(value.intValue());
            return value;
        }

        public List<BigDecimal> trackSource() {
            invocations.incrementAndGet();
            return List.of(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3"));
        }

        public int invocations() {
            return invocations.get();
        }

        public List<Integer> order() {
            return List.copyOf(order);
        }
    }

    public static final class WidgetFunctions {

        private final AtomicInteger invocations = new AtomicInteger();

        public Widget markWidget() {
            invocations.incrementAndGet();
            return new Widget("gadget");
        }

        public int invocations() {
            return invocations.get();
        }
    }

    public record Widget(String name) {
    }

    public static final class OversizedCollectionFunctions {

        public List<BigDecimal> makeOversizedList() {
            return List.of(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3"));
        }
    }
}
