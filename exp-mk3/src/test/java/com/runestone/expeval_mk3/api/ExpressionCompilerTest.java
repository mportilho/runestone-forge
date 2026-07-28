package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    void computesNavigatedCollectionOperationsWithoutLambdaThroughTheCompiledPlan() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .mathContext(new MathContext(4))
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(new BigDecimal("1.0"), new BigDecimal("2.00"), new BigDecimal("3.000")),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(decimal(ExpressionCompiler.compile("items.count()", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("3"));
        assertThat(decimal(ExpressionCompiler.compile("items.sum()", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("6.000"));
        assertThat(decimal(ExpressionCompiler.compile("items.avg()", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("2.000"));
        assertThat(decimal(ExpressionCompiler.compile("one := [1]; empty := one[1:1]; empty.sum()", environment).compute()))
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(decimal(ExpressionCompiler.compile("one := [1]; one.avg()", environment).compute()))
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void evaluatesAllAndAnyLambdasWithEmptyIdentitiesAndShortCircuiting() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        assertThat(ExpressionCompiler.compile("items := [1, 2, 3, 4]; items.all(@ -> lessThanThree(@))", environment)
                .compute()).isEqualTo(false);
        assertThat(functions.invocations()).isEqualTo(3);

        functions.reset();
        assertThat(ExpressionCompiler.compile("items := [1, 2, 3, 4]; items.any(@ -> greaterThanTwo(@))", environment)
                .compute()).isEqualTo(true);
        assertThat(functions.invocations()).isEqualTo(3);

        functions.reset();
        assertThat(ExpressionCompiler.compile("one := [1]; empty := one[1:1]; empty.all(@ -> lessThanThree(@))", environment)
                .compute()).isEqualTo(true);
        assertThat(ExpressionCompiler.compile("one := [1]; empty := one[1:1]; empty.any(@ -> greaterThanTwo(@))", environment)
                .compute()).isEqualTo(false);
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void evaluatesMapAllAndAnyLambdasInCanonicalOrderWithShortCircuiting() {
        CountingFunctions functions = new CountingFunctions();
        Map<String, BigDecimal> source = new HashMap<>();
        source.put("c", new BigDecimal("3"));
        source.put("b", new BigDecimal("2"));
        source.put("A", BigDecimal.ONE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .externalSymbol("m", new MapType(ScalarType.NUMBER), source, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compile("m.all(@ -> lessThanThree(@.v))", environment).compute())
                .isEqualTo(false);
        assertThat(functions.invocations()).isEqualTo(3);

        functions.reset();
        assertThat(ExpressionCompiler.compile("m.any(@ -> isKeyB(@.k))", environment).compute())
                .isEqualTo(true);
        assertThat(functions.seenText()).containsExactly("A", "b");
        assertThat(functions.invocations()).isEqualTo(2);
    }

    @Test
    void mapsCollectionLambdasOncePerItemInOrderAndReturnsAnImmutableCollection() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        Object result = ExpressionCompiler.compile("items := [1, 2, 3]; items.map(@ -> trackNumber(@))", environment)
                .compute();

        assertThat(result).isEqualTo(List.of(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")));
        assertThat(functions.seen()).containsExactly(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"));
        assertThat(functions.invocations()).isEqualTo(3);
        assertThatThrownBy(() -> ((List<Object>) result).add(BigDecimal.TEN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void reducesCollectionsLeftToRightWithTheInitialAccumulatorType() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        assertThat(decimal(ExpressionCompiler.compile(
                        "items := [1, 2, 3]; items.reduce(0, @ -> @.accumulator + trackNumber(@.item))",
                        environment)
                .compute())).isEqualByComparingTo(new BigDecimal("6"));
        assertThat(functions.seen()).containsExactly(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3"));

        assertThat(ExpressionCompiler.compile(
                        "texts := [\"a\", \"b\", \"c\"]; texts.reduce(\"\", @ -> @.accumulator || @.item)",
                        environment)
                .compute()).isEqualTo("abc");
    }

    @Test
    void returnsTheInitialReduceValueForEmptyCollectionsWithoutInvokingTheLambda() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        Object result = ExpressionCompiler.compile(
                        "one := [1]; empty := one[1:1]; empty.reduce(10, @ -> trackNumber(@.item))",
                        environment)
                .compute();

        assertThat(decimal(result)).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void sortsByOneSelectorEvaluationPerElementInSourceOrderAndReturnsAnImmutableCollection() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        Object result = ExpressionCompiler.compile(
                        "items := [3, 1, 2]; items.sortBy(@ -> trackNumber(@), \"asc\")",
                        environment)
                .compute();

        assertThat(result).isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")));
        assertThat(functions.seen()).containsExactly(new BigDecimal("3"), BigDecimal.ONE, new BigDecimal("2"));
        assertThat(functions.invocations()).isEqualTo(3);
        assertThatThrownBy(() -> ((List<Object>) result).add(BigDecimal.TEN))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void sortBySupportsDescendingAndStableOrderingForEqualKeys() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        assertThat(ExpressionCompiler.compile(
                        "texts := [\"b2\", \"a1\", \"b1\"]; texts.sortBy(@ -> firstChar(@), \"asc\")",
                        environment)
                .compute()).isEqualTo(List.of("a1", "b2", "b1"));
        assertThat(ExpressionCompiler.compile(
                        "items := [1, 3, 2]; items.sortBy(@ -> @, \"desc\")",
                        environment)
                .compute()).isEqualTo(List.of(new BigDecimal("3"), new BigDecimal("2"), BigDecimal.ONE));
    }

    @Test
    void sortByAcceptsTemporalSelectorKeyFamilies() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(ExpressionCompiler.compile(
                        "dates := [d\"2024-01-02\", d\"2024-01-01\"]; dates.sortBy(@ -> @, \"asc\")",
                        environment)
                .compute()).isEqualTo(List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));
        assertThat(ExpressionCompiler.compile(
                        "times := [t\"10:30\", t\"09:15\"]; times.sortBy(@ -> @, \"asc\")",
                        environment)
                .compute()).isEqualTo(List.of(LocalTime.of(9, 15), LocalTime.of(10, 30)));
        assertThat(ExpressionCompiler.compile(
                        "datetimes := [dt\"2024-01-02T10:30:00\", dt\"2024-01-01T09:15:00\"]; "
                                + "datetimes.sortBy(@ -> @, \"asc\")",
                        environment)
                .compute()).isEqualTo(List.of(
                        LocalDateTime.of(2024, 1, 1, 9, 15),
                        LocalDateTime.of(2024, 1, 2, 10, 30)));
    }

    @Test
    void rejectsInvalidReduceAndSortByContractsBeforeRuntime() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThatThrownBy(() -> ExpressionCompiler.compile(
                        "items := [1]; items.reduce(0, @ -> \"x\")",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile(
                        "items := [1]; items.sortBy(@ -> @, \"up\")",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile(
                        "items := [true]; items.sortBy(@ -> @, \"asc\")",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile(
                        "items := [1]; items.sortBy(@ -> items?.sum(), \"asc\")",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
    }

    @Test
    void checksSortByMaterializationLimitBeforeInvokingTheSelector() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .maxMaterializedSize(2)
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, new BigDecimal("2")),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compile(
                "items.sortBy(@ -> trackNumber(@), \"asc\")", environment);

        assertThatThrownBy(() -> expression.compute(Map.of(
                        "items", List.of(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void exposesMapEntriesToLambdasInCanonicalOrderWithoutPreservingKeys() {
        Map<String, BigDecimal> source = new HashMap<>();
        source.put("b", new BigDecimal("2"));
        source.put("A", BigDecimal.ONE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), source, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compile("m.map(@ -> @.k)", environment).compute())
                .isEqualTo(List.of("A", "b"));
        assertThat(ExpressionCompiler.compile("m.map(@ -> @.v + 10)", environment).compute())
                .isEqualTo(List.of(new BigDecimal("11"), new BigDecimal("12")));
    }

    @Test
    void nestedLambdasUseTheInnermostCurrentItemAndRespectDepthLimits() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "outer",
                        new CollectionType(new CollectionType(ScalarType.NUMBER)),
                        List.of(List.of(BigDecimal.ONE, new BigDecimal("2")), List.of(new BigDecimal("3"))),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compile("outer.map(@ -> @[?(@ > 1)].count())", environment).compute())
                .isEqualTo(List.of(BigDecimal.ONE, BigDecimal.ONE));

        ExpressionEnvironment shallowEnvironment = ExpressionEnvironment.builder()
                .maxCurrentItemDepth(1)
                .build();
        assertThatThrownBy(() -> ExpressionCompiler.compile("outer := [[1]]; outer.map(@ -> @.map(@ -> @))", shallowEnvironment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED"));
    }

    @Test
    void rejectsInvalidLambdaResults() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        assertThatThrownBy(() -> ExpressionCompiler.compile("items := [1]; items.all(@ -> @)", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile("items := [1]; items.map(@ -> items?.sum())", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void rejectsEscapedMapEntryLambdaResultsAndEntryEquality() {
        Map<String, BigDecimal> source = new HashMap<>();
        source.put("a", BigDecimal.ONE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), source, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThatThrownBy(() -> ExpressionCompiler.compile("m.map(@ -> [@])", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile("m.any(@ -> @ = @)", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OBJECT_EQUALITY_NOT_SUPPORTED"));
    }

    @Test
    void rejectsLambdaArgumentsForGlobalFunctions() {
        assertThatThrownBy(() -> ExpressionCompiler.compile("unknown(@ -> @)", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_LAMBDA_ARGUMENT_UNSUPPORTED"));
    }

    @Test
    void checksMapMaterializationBeforeInvokingLambda() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .maxMaterializedSize(2)
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, new BigDecimal("2")),
                        ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        CompiledExpression expression = ExpressionCompiler.compile("items.map(@ -> trackNumber(@))", environment);

        assertThatThrownBy(() -> expression.compute(Map.of(
                        "items", List.of(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void safeMapCallPreservesTheRealReceiverBehaviorWhenNotNull() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(ExpressionCompiler.compile("items := [1, 2]; items?.map(@ -> @ + 1)", environment).compute())
                .isEqualTo(List.of(new BigDecimal("2"), new BigDecimal("3")));
        assertThat(ExpressionCompiler.compile("one := [1]; empty := one[1:1]; empty.map(@ -> @ + 1)", environment)
                .compute()).isEqualTo(List.of());
    }

    @Test
    void materializesNavigatedMapOperationsInCanonicalOrder() {
        Map<String, BigDecimal> source = new HashMap<>();
        source.put("b", new BigDecimal("2"));
        source.put("A", BigDecimal.ONE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), source, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(decimal(ExpressionCompiler.compile("m.count()", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("2"));
        assertThat(ExpressionCompiler.compile("m.keys()", environment).compute())
                .isEqualTo(List.of("A", "b"));
        assertThat(ExpressionCompiler.compile("m.values()", environment).compute())
                .isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2")));
        assertThatThrownBy(() -> ((List<Object>) ExpressionCompiler.compile("m.keys()", environment).compute()).add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void materializesWildcardNavigationForCollectionsMapsAndRegisteredObjects() {
        Map<String, BigDecimal> source = new HashMap<>();
        source.put("b", new BigDecimal("2"));
        source.put("A", BigDecimal.ONE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, "second", "first")
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, new BigDecimal("2")),
                        ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("m", new MapType(ScalarType.NUMBER), source, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("object", new WildcardChildProvider(), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        Object collectionResult = ExpressionCompiler.compile("items[*]", environment).compute();
        assertThat(collectionResult).isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2")));
        assertThatThrownBy(() -> ((List<Object>) collectionResult).add(BigDecimal.TEN))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(ExpressionCompiler.compile("m[*]", environment).compute())
                .isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2")));
        assertThat(ExpressionCompiler.compile("object[*]", environment).compute())
                .isEqualTo(List.of(new BigDecimal("2"), BigDecimal.ONE));
    }

    @Test
    void unorderedObjectWildcardMetadataUsesBinaryMemberOrderAtRuntime() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, Set.of("second", "first"))
                .externalSymbol("object", new WildcardChildProvider(), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compile("object[*]", environment).compute())
                .isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2")));
    }

    @Test
    void objectWildcardNavigationDoesNotMaskAccessorFailuresOrMaterializationLimits() {
        ExpressionEnvironment failingAccessor = ExpressionEnvironment.builder()
                .registerJavaTypeWildcardChildren(FailingWildcardChildProvider.class, "first")
                .externalSymbol("object", new FailingWildcardChildProvider(), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        assertThatThrownBy(() -> ExpressionCompiler.compile("object?.[*]", failingAccessor).compute())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("first failed");

        ExpressionEnvironment limited = ExpressionEnvironment.builder()
                .maxMaterializedSize(1)
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, "first", "second")
                .externalSymbol("object", new WildcardChildProvider(), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        assertThatThrownBy(() -> ExpressionCompiler.compile("object[*]", limited))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_MATERIALIZATION_LIMIT_EXCEEDED"));
    }

    @Test
    void rejectsInvalidNavigatedCollectionOperationCallsBeforeRuntime() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThatThrownBy(() -> ExpressionCompiler.compile("sum([1])", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_UNKNOWN_FUNCTION"));
        assertThatThrownBy(() -> ExpressionCompiler.compile("items := [1]; items.keys()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile("m.sum()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile("texts := [\"x\"]; texts.sum()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile("items := [1]; empty := items[1:1]; empty.avg()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile("items := [1]; items.sum(2)", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThatThrownBy(() -> ExpressionCompiler.compile("items := [true]; items.all()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
    }

    @Test
    void safeNavigatedCollectionOperationKeepsTheRealReceiverBehaviorWhenNotNull() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items",
                        new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, new BigDecimal("2")),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(decimal(ExpressionCompiler.compile("items?.sum()", environment).compute()))
                .isEqualByComparingTo(new BigDecimal("3"));
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
        private final List<BigDecimal> seen = new ArrayList<>();
        private final List<String> seenText = new ArrayList<>();

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

        public Boolean lessThanThree(BigDecimal value) {
            invocations.incrementAndGet();
            return value.compareTo(new BigDecimal("3")) < 0;
        }

        public Boolean greaterThanTwo(BigDecimal value) {
            invocations.incrementAndGet();
            return value.compareTo(new BigDecimal("2")) > 0;
        }

        public BigDecimal trackNumber(BigDecimal value) {
            invocations.incrementAndGet();
            seen.add(value);
            return value;
        }

        public Boolean isKeyB(String value) {
            invocations.incrementAndGet();
            seenText.add(value);
            return "b".equals(value);
        }

        public String firstChar(String value) {
            invocations.incrementAndGet();
            seenText.add(value);
            return value.substring(0, 1);
        }

        private int invocations() {
            return invocations.get();
        }

        private List<BigDecimal> seen() {
            return List.copyOf(seen);
        }

        private List<String> seenText() {
            return List.copyOf(seenText);
        }

        private void reset() {
            invocations.set(0);
            seen.clear();
            seenText.clear();
        }
    }

    public static final class WildcardChildProvider {

        public BigDecimal first() {
            return BigDecimal.ONE;
        }

        public BigDecimal second() {
            return new BigDecimal("2");
        }
    }

    public static final class FailingWildcardChildProvider {

        public BigDecimal first() {
            throw new IllegalStateException("first failed");
        }
    }

    private static BigDecimal decimal(Object value) {
        return (BigDecimal) value;
    }
}
