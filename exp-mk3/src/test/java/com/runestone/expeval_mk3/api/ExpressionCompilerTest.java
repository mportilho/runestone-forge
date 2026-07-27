package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionCompilerTest {

    @Test
    void compilesAndComputesAnImmutableCollectionLiteral() {
        CompiledExpression expression = ExpressionCompiler.compile("[1, 2.5]", ExpressionEnvironment.standard());

        Object result = expression.compute();

        assertThat(result).isEqualTo(List.of(new BigDecimal("1"), new BigDecimal("2.5")));
        assertThatThrownBy(() -> ((List<Object>) result).add(BigDecimal.ZERO))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void computesScalarLiteralsWithCanonicalPublicValues() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(ExpressionCompiler.compile("42", environment).compute()).isEqualTo(new BigDecimal("42"));
        assertThat(ExpressionCompiler.compile("2.50", environment).compute()).isEqualTo(new BigDecimal("2.50"));
        assertThat(ExpressionCompiler.compile("true", environment).compute()).isEqualTo(true);
        assertThat(ExpressionCompiler.compile("\"text\"", environment).compute()).isEqualTo("text");
        assertThat(ExpressionCompiler.compile("d\"2024-01-02\"", environment).compute())
                .isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(ExpressionCompiler.compile("t\"10:30\"", environment).compute())
                .isEqualTo(LocalTime.of(10, 30));
        assertThat(ExpressionCompiler.compile("dt\"2024-01-02T10:30:00\"", environment).compute())
                .isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 30));
    }

    @Test
    void comparesCollectionsStructurallyAndInOrder() {
        assertThat(ExpressionCompiler.compile("[1, 2.0] = [1.00, 2]", ExpressionEnvironment.standard()).compute())
                .isEqualTo(true);
        assertThat(ExpressionCompiler.compile("[1, 2] = [2, 1]", ExpressionEnvironment.standard()).compute())
                .isEqualTo(false);
        assertThat(ExpressionCompiler.compile("[1, 2] <> [1, 3]", ExpressionEnvironment.standard()).compute())
                .isEqualTo(true);
    }

    @Test
    void infersAnEmptyCollectionFromItsEqualityOperand() {
        assertThat(ExpressionCompiler.compile("[] = [1]", ExpressionEnvironment.standard()).compute())
                .isEqualTo(false);
        assertThat(ExpressionCompiler.compile("[1] <> []", ExpressionEnvironment.standard()).compute())
                .isEqualTo(true);
    }

    @Test
    void comparesMapsByKeysAndStructuralValuesRegardlessOfJavaOrder() {
        Map<String, Object> left = new LinkedHashMap<>();
        left.put("first", List.of(new BigDecimal("1.0")));
        left.put("second", List.of(new BigDecimal("2")));
        Map<String, Object> right = new LinkedHashMap<>();
        right.put("second", List.of(new BigDecimal("2.00")));
        right.put("first", List.of(new BigDecimal("1")));
        ExpressionType mapType = new MapType(new CollectionType(ScalarType.NUMBER));
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("left", mapType, left, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("right", mapType, right, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compile("left = right", environment).compute()).isEqualTo(true);
        Object publicMap = ExpressionCompiler.compile("left", environment).compute();
        assertThatThrownBy(() -> ((Map<String, Object>) publicMap).put("third", List.of(BigDecimal.TEN)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void enforcesTheMaterializationLimitAtCompilation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().maxMaterializedSize(2).build();

        assertThat(ExpressionCompiler.compile("[1, 2]", environment).compute()).isEqualTo(List.of(
                new BigDecimal("1"), new BigDecimal("2")));
        assertThatThrownBy(() -> ExpressionCompiler.compile("[1, 2, 3]", environment))
                .isInstanceOf(ExpressionCompilationException.class)
                .satisfies(error -> assertThat(((ExpressionCompilationException) error).diagnostics().getFirst().code())
                        .isEqualTo("SEMANTIC_MATERIALIZATION_LIMIT_EXCEEDED"));
    }

    @Test
    void usesAnIsolatedSnapshotForEachComputation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ZERO),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compile("items", environment);
        List<BigDecimal> override = new java.util.ArrayList<>(List.of(BigDecimal.ONE));

        Object first = expression.compute(Map.of("items", override));
        override.add(BigDecimal.TWO);

        assertThat(first).isEqualTo(List.of(BigDecimal.ONE));
        assertThat(expression.compute()).isEqualTo(List.of(BigDecimal.ZERO));
        assertThatThrownBy(() -> ((List<Object>) first).add(BigDecimal.TEN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void reusesOneCompiledExpressionAcrossConcurrentComputations() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ZERO),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compile("items", environment);

        List<Object> results = IntStream.range(0, 100)
                .parallel()
                .mapToObj(value -> expression.compute(Map.of("items", List.of(BigDecimal.valueOf(value)))))
                .toList();

        assertThat(results).containsExactlyElementsOf(IntStream.range(0, 100)
                .mapToObj(value -> List.of(BigDecimal.valueOf(value)))
                .toList());
    }

    @Test
    void preservesTheRootDiagnosticForAnInvalidCollectionElement() {
        assertThatThrownBy(() -> ExpressionCompiler.compile("[missing]", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic ->
                                assertThat(diagnostic.code()).isEqualTo("SEMANTIC_UNKNOWN_SYMBOL")));
    }

    @Test
    void computesScalarOperatorsThroughTheCompiledPlan() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(decimal(ExpressionCompiler.compile("1 + 2 * 3 - 4 / 2", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("5"));
        assertThat(decimal(ExpressionCompiler.compile("2^-3", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("0.125"));
        assertThat(decimal(ExpressionCompiler.compile("50%", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(decimal(ExpressionCompiler.compile("5!", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("120"));
        assertThat(ExpressionCompiler.compile("\"run\" || \"estone\"", environment).compute()).isEqualTo("runestone");
        assertThat(ExpressionCompiler.compile("3 between 1 and 5", environment).compute()).isEqualTo(true);
        assertThat(ExpressionCompiler.compile("\"abc123\" =~ \"[a-z]+\\\\d+\"", environment).compute()).isEqualTo(true);
        assertThat(ExpressionCompiler.compile("2 in [1, 2, 3]", environment).compute()).isEqualTo(true);
    }

    @Test
    void usesStableSlotsForDefaultsAndOverrides() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("fixed", new BigDecimal("10"), ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("input", new BigDecimal("2"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compile("fixed + input * 3", environment);

        assertThat(decimal(expression.compute())).isEqualByComparingTo(new BigDecimal("16"));
        assertThat(decimal(expression.compute(Map.of("input", new BigDecimal("4")))))
                .isEqualByComparingTo(new BigDecimal("22"));
    }

    @Test
    void computesAssignmentsConditionalsAndBoundFunctions() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .externalSymbol("base", new BigDecimal("4"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compile("x := base + 1; if(x > 5, bump(x), x)", environment);

        assertThat(decimal(expression.compute())).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(functions.invocations()).isZero();

        assertThat(decimal(expression.compute(Map.of("base", new BigDecimal("6")))))
                .isEqualByComparingTo(new BigDecimal("8"));
        assertThat(functions.invocations()).isOne();
    }

    @Test
    void indexesSlicesAndFiltersCollectionValuesThroughTheCompiledPlan() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        new BigDecimal[] {BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")},
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(decimal(ExpressionCompiler.compile("items[0]", environment).compute()))
                .isEqualByComparingTo(BigDecimal.ONE);
        assertThat(decimal(ExpressionCompiler.compile("items[-1]", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("3"));
        assertThat(ExpressionCompiler.compile("items[1:]", environment).compute())
                .isEqualTo(List.of(new BigDecimal("2"), new BigDecimal("3")));
        assertThat(ExpressionCompiler.compile("items[?(@ > 1)]", environment).compute())
                .isEqualTo(List.of(new BigDecimal("2"), new BigDecimal("3")));
    }

    @Test
    void destructuresCollectionPrefixesAndRejectsInvalidTargets() {
        assertThat(decimal(ExpressionCompiler.compile("[a, b] := [10, 20, 30]; a + b", ExpressionEnvironment.standard())
                .compute()))
                .isEqualByComparingTo(new BigDecimal("30"));

        assertThatThrownBy(() -> ExpressionCompiler.compile("[a, a] := [1, 2]; a", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_DUPLICATE_ASSIGNMENT_TARGET"));

        assertThatThrownBy(() -> ExpressionCompiler.compile("[a, b, c] := [1, 2]; a", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_DESTRUCTURING_SIZE_MISMATCH"));
    }

    @Test
    void defersDestructuringMinimumSizeChecksForDynamicCollections() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, new BigDecimal("2")),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compile("[a, b] := items; a", environment);

        assertThat(decimal(expression.compute())).isEqualByComparingTo(BigDecimal.ONE);
        assertThatThrownBy(() -> expression.compute(Map.of("items", List.of(BigDecimal.ONE))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("destructuring source does not contain enough elements");
    }

    @Test
    void propagatesSliceCardinalityAndDefersFilterCardinality() {
        assertThatThrownBy(() -> ExpressionCompiler.compile(
                        "items := [1, 2, 3, 4]; [a, b, c] := items[1:3]; a",
                        ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_DESTRUCTURING_SIZE_MISMATCH"));

        CompiledExpression expression = ExpressionCompiler.compile(
                "items := [1, 2]; [a, b] := items[?(@ > 1)]; a",
                ExpressionEnvironment.standard());
        assertThatThrownBy(expression::compute)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("destructuring source does not contain enough elements");
    }

    @Test
    void nestedFiltersUseTheInnermostCurrentItem() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "outer",
                        new CollectionType(new CollectionType(ScalarType.NUMBER)),
                        List.of(List.of(BigDecimal.ONE, new BigDecimal("2")), List.of(new BigDecimal("3"))),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        Object result = ExpressionCompiler.compile("outer[?(@[?(@ > 2)] = [3])]", environment).compute();

        assertThat(result).isEqualTo(List.of(List.of(new BigDecimal("3"))));
    }

    @Test
    void enforcesCurrentItemDepthForNestedFilters() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .maxCurrentItemDepth(1)
                .build();

        assertThatThrownBy(() -> ExpressionCompiler.compile(
                        "outer := [[1]]; inner := [1]; outer[?(inner[?(@ = 1)] = [1])]",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED"));
    }

    @Test
    void preservesShortCircuitAndEagerEvaluationContracts() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        assertThat(ExpressionCompiler.compile("false and markTrue()", environment).compute()).isEqualTo(false);
        assertThat(functions.invocations()).isZero();

        assertThat(ExpressionCompiler.compile("true or markTrue()", environment).compute()).isEqualTo(true);
        assertThat(functions.invocations()).isZero();

        assertThat(ExpressionCompiler.compile("false xor markTrue()", environment).compute()).isEqualTo(true);
        assertThat(functions.invocations()).isOne();

        assertThat(ExpressionCompiler.compile("false nand markTrue()", environment).compute()).isEqualTo(true);
        assertThat(functions.invocations()).isEqualTo(2);

        assertThat(ExpressionCompiler.compile("true nor markTrue()", environment).compute()).isEqualTo(false);
        assertThat(functions.invocations()).isEqualTo(3);

        assertThat(ExpressionCompiler.compile("1 ?? markNumber()", environment).compute()).isEqualTo(new BigDecimal("1"));
        assertThat(functions.invocations()).isEqualTo(3);

        assertThat(ExpressionCompiler.compile("0 between 1 and markNumber()", environment).compute()).isEqualTo(false);
        assertThat(functions.invocations()).isEqualTo(3);
    }

    public static final class CountingFunctions {

        private final AtomicInteger invocations = new AtomicInteger();

        public BigDecimal bump(BigDecimal value) {
            invocations.incrementAndGet();
            return value.add(BigDecimal.ONE);
        }

        public Boolean markTrue() {
            invocations.incrementAndGet();
            return true;
        }

        public BigDecimal markNumber() {
            invocations.incrementAndGet();
            return BigDecimal.TEN;
        }

        private int invocations() {
            return invocations.get();
        }
    }

    private static BigDecimal decimal(Object value) {
        return (BigDecimal) value;
    }
}
