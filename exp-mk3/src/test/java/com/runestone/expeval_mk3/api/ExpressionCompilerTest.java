package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
}
