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
    void compileOrThrowReturnsAUsableCompiledExpressionOnSuccess() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("1 + 2", ExpressionEnvironment.standard());

        assertThat(expression.asResult().compute()).isEqualTo(new BigDecimal("3"));
    }

    @Test
    void compileOrThrowThrowsExpressionCompilationExceptionOnParseFailure() {
        assertThatThrownBy(() -> ExpressionCompiler.compileOrThrow("1 +", ExpressionEnvironment.standard()))
                .isInstanceOf(ExpressionCompilationException.class);
    }

    @Test
    void compileOrThrowThrowsExpressionCompilationExceptionOnSemanticFailure() {
        assertThatThrownBy(() -> ExpressionCompiler.compileOrThrow("missing", ExpressionEnvironment.standard()))
                .isInstanceOf(ExpressionCompilationException.class);
    }

    @Test
    void compileOrThrowSucceedsForAnAssignmentOnlyFileButItsAsResultViewRejectsTheMissingResult() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("x := 1;", ExpressionEnvironment.standard());

        assertThatThrownBy(expression::asResult)
                .isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                        assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.NO_RESULT_EXPRESSION));
        assertThat(expression.asAssignments().compute()).isEqualTo(Map.of("x", new BigDecimal("1")));
    }

    @Test
    void compileReturnsACleanSuccessWithNoWarnings() {
        ExpressionCompilationResult result = ExpressionCompiler.compile("1 + 2", ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(ExpressionCompilationResult.Success.class, success -> {
            assertThat(success.diagnostics()).isEmpty();
            assertThat(success.compiledExpression().compilationDiagnostics()).isEmpty();
            assertThat(success.compiledExpression().asResult().compute()).isEqualTo(new BigDecimal("3"));
        });
    }

    @Test
    void compileReturnsASuccessCarryingTheSymbolShadowingWarningOnBothTheResultAndTheArtifact() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        ExpressionCompilationResult result = ExpressionCompiler.compile("x := 1; x", environment);

        assertThat(result).isInstanceOfSatisfying(ExpressionCompilationResult.Success.class, success -> {
            assertThat(success.diagnostics()).singleElement().satisfies(diagnostic -> {
                assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.WARNING);
                assertThat(diagnostic.code()).isEqualTo("SEMANTIC_SYMBOL_SHADOWING");
            });
            assertThat(success.compiledExpression().compilationDiagnostics()).isEqualTo(success.diagnostics());
        });
    }

    @Test
    void compileReturnsAFailureOnParsingErrorsWithoutThrowing() {
        ExpressionCompilationResult result = ExpressionCompiler.compile("1 +", ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                assertThat(failure.diagnostics()).isNotEmpty().allSatisfy(diagnostic ->
                        assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.ERROR)));
    }

    @Test
    void compileReturnsAFailureOnSemanticErrorsWithoutThrowing() {
        ExpressionCompilationResult result = ExpressionCompiler.compile("missing", ExpressionEnvironment.standard());

        assertThat(result).isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic ->
                        assertThat(diagnostic.code()).isEqualTo("SEMANTIC_UNKNOWN_SYMBOL")));
    }

    @Test
    void compileFailureRetainsBothWarningsAndErrorsWhenBothAreProduced() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        ExpressionCompilationResult result = ExpressionCompiler.compile("x := 1; x + missing", environment);

        assertThat(result).isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure -> {
            assertThat(failure.diagnostics()).anySatisfy(diagnostic ->
                    assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.WARNING));
            assertThat(failure.diagnostics()).anySatisfy(diagnostic ->
                    assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.ERROR));
        });
    }

    @Test
    void compileSucceedsWithWarningsForTheAssignmentsOnlyNoResultExpressionCase() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        ExpressionCompilationResult result = ExpressionCompiler.compile("x := 1;", environment);

        assertThat(result).isInstanceOfSatisfying(ExpressionCompilationResult.Success.class, success -> {
            assertThat(success.diagnostics()).singleElement().satisfies(diagnostic ->
                    assertThat(diagnostic.code()).isEqualTo("SEMANTIC_SYMBOL_SHADOWING"));
            assertThat(success.compiledExpression().asAssignments().compute())
                    .isEqualTo(Map.of("x", new BigDecimal("1")));
        });
    }

    @Test
    void compilationResultDiagnosticListsAreImmutable() {
        ExpressionCompilationResult.Success success = (ExpressionCompilationResult.Success)
                ExpressionCompiler.compile("1 + 2", ExpressionEnvironment.standard());
        ExpressionCompilationResult.Failure failure = (ExpressionCompilationResult.Failure)
                ExpressionCompiler.compile("1 +", ExpressionEnvironment.standard());

        assertThatThrownBy(() -> success.diagnostics().add(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> success.compiledExpression().compilationDiagnostics().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> failure.diagnostics().add(null)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void compileOrThrowReturnsAnArtifactWithTheSameWarningsAsResultOrientedCompilation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.TEN, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        CompiledExpression expression = ExpressionCompiler.compileOrThrow("x := 1; x", environment);

        assertThat(expression.compilationDiagnostics()).singleElement().satisfies(diagnostic ->
                assertThat(diagnostic.code()).isEqualTo("SEMANTIC_SYMBOL_SHADOWING"));
    }

    @Test
    void compileOrThrowExceptionExposesTheSameFailureDiagnosticsAsResultOrientedCompilation() {
        assertThatThrownBy(() -> ExpressionCompiler.compileOrThrow("missing", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationException.class, exception ->
                        assertThat(exception.diagnostics()).singleElement().satisfies(diagnostic ->
                                assertThat(diagnostic.code()).isEqualTo("SEMANTIC_UNKNOWN_SYMBOL")));
    }

    @Test
    void compilesAndComputesAnImmutableCollectionLiteral() {
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("[1, 2.5]", ExpressionEnvironment.standard());

        Object result = expression.asResult().compute();

        assertThat(result).isEqualTo(List.of(new BigDecimal("1"), new BigDecimal("2.5")));
        assertThatThrownBy(() -> ((List<Object>) result).add(BigDecimal.ZERO))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void computesScalarLiteralsWithCanonicalPublicValues() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(ExpressionCompiler.compileOrThrow("42", environment).asMath().compute()).isEqualByComparingTo(new BigDecimal("42"));
        assertThat(ExpressionCompiler.compileOrThrow("2.50", environment).asMath().compute()).isEqualByComparingTo(new BigDecimal("2.50"));
        assertThat(ExpressionCompiler.compileOrThrow("true", environment).asLogical().compute()).isEqualTo(true);
        assertThat(ExpressionCompiler.compileOrThrow("\"text\"", environment).asResult().compute()).isEqualTo("text");
        assertThat(ExpressionCompiler.compileOrThrow("d\"2024-01-02\"", environment).asResult().compute())
                .isEqualTo(LocalDate.of(2024, 1, 2));
        assertThat(ExpressionCompiler.compileOrThrow("t\"10:30\"", environment).asResult().compute())
                .isEqualTo(LocalTime.of(10, 30));
        assertThat(ExpressionCompiler.compileOrThrow("dt\"2024-01-02T10:30:00\"", environment).asResult().compute())
                .isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 30));
    }

    @Test
    void comparesCollectionsStructurallyAndInOrder() {
        assertThat(ExpressionCompiler.compileOrThrow("[1, 2.0] = [1.00, 2]", ExpressionEnvironment.standard()).asLogical().compute())
                .isEqualTo(true);
        assertThat(ExpressionCompiler.compileOrThrow("[1, 2] = [2, 1]", ExpressionEnvironment.standard()).asLogical().compute())
                .isEqualTo(false);
        assertThat(ExpressionCompiler.compileOrThrow("[1, 2] <> [1, 3]", ExpressionEnvironment.standard()).asLogical().compute())
                .isEqualTo(true);
    }

    @Test
    void infersAnEmptyCollectionFromItsEqualityOperand() {
        assertThat(ExpressionCompiler.compileOrThrow("[] = [1]", ExpressionEnvironment.standard()).asLogical().compute())
                .isEqualTo(false);
        assertThat(ExpressionCompiler.compileOrThrow("[1] <> []", ExpressionEnvironment.standard()).asLogical().compute())
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

        assertThat(ExpressionCompiler.compileOrThrow("left = right", environment).asLogical().compute()).isEqualTo(true);
        Object publicMap = ExpressionCompiler.compileOrThrow("left", environment).asResult().compute();
        assertThatThrownBy(() -> ((Map<String, Object>) publicMap).put("third", List.of(BigDecimal.TEN)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void enforcesTheMaterializationLimitAtCompilation() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().maxMaterializedSize(2).build();

        assertThat(ExpressionCompiler.compileOrThrow("[1, 2]", environment).asResult().compute()).isEqualTo(List.of(
                new BigDecimal("1"), new BigDecimal("2")));
        assertThat(ExpressionCompiler.compile("[1, 2, 3]", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
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
        ResultExpression expression = ExpressionCompiler.compileOrThrow("items", environment).asResult();
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
        ResultExpression expression = ExpressionCompiler.compileOrThrow("items", environment).asResult();

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
        assertThat(ExpressionCompiler.compile("[missing]", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics()).singleElement().satisfies(diagnostic ->
                                assertThat(diagnostic.code()).isEqualTo("SEMANTIC_UNKNOWN_SYMBOL")));
    }

    @Test
    void computesScalarOperatorsThroughTheCompiledPlan() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(ExpressionCompiler.compileOrThrow("1 + 2 * 3 - 4 / 2", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("5"));
        assertThat(ExpressionCompiler.compileOrThrow("2^-3", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("0.125"));
        assertThat(ExpressionCompiler.compileOrThrow("50%", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(ExpressionCompiler.compileOrThrow("5!", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("120"));
        assertThat(ExpressionCompiler.compileOrThrow("\"run\" || \"estone\"", environment).asResult().compute()).isEqualTo("runestone");
        assertThat(ExpressionCompiler.compileOrThrow("3 between 1 and 5", environment).asLogical().compute()).isEqualTo(true);
        assertThat(ExpressionCompiler.compileOrThrow("\"abc123\" =~ \"[a-z]+\\\\d+\"", environment).asLogical().compute()).isEqualTo(true);
        assertThat(ExpressionCompiler.compileOrThrow("2 in [1, 2, 3]", environment).asLogical().compute()).isEqualTo(true);
    }

    @Test
    void usesStableSlotsForDefaultsAndOverrides() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("fixed", new BigDecimal("10"), ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("input", new BigDecimal("2"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        MathExpression expression = ExpressionCompiler.compileOrThrow("fixed + input * 3", environment).asMath();

        assertThat(expression.compute()).isEqualByComparingTo(new BigDecimal("16"));
        assertThat(expression.compute(Map.of("input", new BigDecimal("4"))))
                .isEqualByComparingTo(new BigDecimal("22"));
    }

    @Test
    void overridingADeclaredButUnreferencedExternalSymbolIsAcceptedAndHasNoEffect() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("used", new BigDecimal("1"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("unused", new BigDecimal("1"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        MathExpression expression = ExpressionCompiler.compileOrThrow("used + 2", environment).asMath();

        assertThat(expression.compute(Map.of("unused", new BigDecimal("999"))))
                .isEqualByComparingTo(new BigDecimal("3"));
    }

    @Test
    void overridingADeclaredButUnreferencedNonOverridableExternalSymbolStillFails() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("used", new BigDecimal("1"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("unused", new BigDecimal("1"), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        MathExpression expression = ExpressionCompiler.compileOrThrow("used + 2", environment).asMath();

        assertThatThrownBy(() -> expression.compute(Map.of("unused", new BigDecimal("999"))))
                .isInstanceOf(ExpressionExecutionException.class);
    }

    @Test
    void computesAssignmentsConditionalsAndBoundFunctions() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .externalSymbol("base", new BigDecimal("4"), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        MathExpression expression = ExpressionCompiler.compileOrThrow("x := base + 1; if(x > 5, bump(x), x)", environment).asMath();

        assertThat(expression.compute()).isEqualByComparingTo(new BigDecimal("5"));
        assertThat(functions.invocations()).isZero();

        assertThat(expression.compute(Map.of("base", new BigDecimal("6"))))
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

        assertThat(ExpressionCompiler.compileOrThrow("items[0]", environment).asMath().compute())
                .isEqualByComparingTo(BigDecimal.ONE);
        assertThat(ExpressionCompiler.compileOrThrow("items[-1]", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("3"));
        assertThat(ExpressionCompiler.compileOrThrow("items[1:]", environment).asResult().compute())
                .isEqualTo(List.of(new BigDecimal("2"), new BigDecimal("3")));
        assertThat(ExpressionCompiler.compileOrThrow("items[?(@ > 1)]", environment).asResult().compute())
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

        assertThat(ExpressionCompiler.compileOrThrow("items.count()", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("3"));
        assertThat(ExpressionCompiler.compileOrThrow("items.sum()", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("6.000"));
        assertThat(ExpressionCompiler.compileOrThrow("items.avg()", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("2.000"));
        assertThat(ExpressionCompiler.compileOrThrow("one := [1]; empty := one[1:1]; empty.sum()", environment).asMath().compute())
                .isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(ExpressionCompiler.compileOrThrow("one := [1]; one.avg()", environment).asMath().compute())
                .isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void evaluatesAllAndAnyLambdasWithEmptyIdentitiesAndShortCircuiting() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        assertThat(ExpressionCompiler.compileOrThrow("items := [1, 2, 3, 4]; items.all(@ -> lessThanThree(@))", environment)
                .asLogical().compute()).isEqualTo(false);
        assertThat(functions.invocations()).isEqualTo(3);

        functions.reset();
        assertThat(ExpressionCompiler.compileOrThrow("items := [1, 2, 3, 4]; items.any(@ -> greaterThanTwo(@))", environment)
                .asLogical().compute()).isEqualTo(true);
        assertThat(functions.invocations()).isEqualTo(3);

        functions.reset();
        assertThat(ExpressionCompiler.compileOrThrow("one := [1]; empty := one[1:1]; empty.all(@ -> lessThanThree(@))", environment)
                .asLogical().compute()).isEqualTo(true);
        assertThat(ExpressionCompiler.compileOrThrow("one := [1]; empty := one[1:1]; empty.any(@ -> greaterThanTwo(@))", environment)
                .asLogical().compute()).isEqualTo(false);
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

        assertThat(ExpressionCompiler.compileOrThrow("m.all(@ -> lessThanThree(@.v))", environment).asLogical().compute())
                .isEqualTo(false);
        assertThat(functions.invocations()).isEqualTo(3);

        functions.reset();
        assertThat(ExpressionCompiler.compileOrThrow("m.any(@ -> isKeyB(@.k))", environment).asLogical().compute())
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

        Object result = ExpressionCompiler.compileOrThrow("items := [1, 2, 3]; items.map(@ -> trackNumber(@))", environment)
                .asResult().compute();

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

        assertThat(ExpressionCompiler.compileOrThrow(
                        "items := [1, 2, 3]; items.reduce(0, @ -> @.accumulator + trackNumber(@.item))",
                        environment)
                .asMath().compute()).isEqualByComparingTo(new BigDecimal("6"));
        assertThat(functions.seen()).containsExactly(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3"));

        assertThat(ExpressionCompiler.compileOrThrow(
                        "texts := [\"a\", \"b\", \"c\"]; texts.reduce(\"\", @ -> @.accumulator || @.item)",
                        environment)
                .asResult().compute()).isEqualTo("abc");
    }

    @Test
    void returnsTheInitialReduceValueForEmptyCollectionsWithoutInvokingTheLambda() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        BigDecimal result = ExpressionCompiler.compileOrThrow(
                        "one := [1]; empty := one[1:1]; empty.reduce(10, @ -> trackNumber(@.item))",
                        environment)
                .asMath().compute();

        assertThat(result).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void sortsByOneSelectorEvaluationPerElementInSourceOrderAndReturnsAnImmutableCollection() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        Object result = ExpressionCompiler.compileOrThrow(
                        "items := [3, 1, 2]; items.sortBy(@ -> trackNumber(@), \"asc\")",
                        environment)
                .asResult().compute();

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

        assertThat(ExpressionCompiler.compileOrThrow(
                        "texts := [\"b2\", \"a1\", \"b1\"]; texts.sortBy(@ -> firstChar(@), \"asc\")",
                        environment)
                .asResult().compute()).isEqualTo(List.of("a1", "b2", "b1"));
        assertThat(ExpressionCompiler.compileOrThrow(
                        "items := [1, 3, 2]; items.sortBy(@ -> @, \"desc\")",
                        environment)
                .asResult().compute()).isEqualTo(List.of(new BigDecimal("3"), new BigDecimal("2"), BigDecimal.ONE));
    }

    @Test
    void sortByAcceptsTemporalSelectorKeyFamilies() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(ExpressionCompiler.compileOrThrow(
                        "dates := [d\"2024-01-02\", d\"2024-01-01\"]; dates.sortBy(@ -> @, \"asc\")",
                        environment)
                .asResult().compute()).isEqualTo(List.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2)));
        assertThat(ExpressionCompiler.compileOrThrow(
                        "times := [t\"10:30\", t\"09:15\"]; times.sortBy(@ -> @, \"asc\")",
                        environment)
                .asResult().compute()).isEqualTo(List.of(LocalTime.of(9, 15), LocalTime.of(10, 30)));
        assertThat(ExpressionCompiler.compileOrThrow(
                        "datetimes := [dt\"2024-01-02T10:30:00\", dt\"2024-01-01T09:15:00\"]; "
                                + "datetimes.sortBy(@ -> @, \"asc\")",
                        environment)
                .asResult().compute()).isEqualTo(List.of(
                        LocalDateTime.of(2024, 1, 1, 9, 15),
                        LocalDateTime.of(2024, 1, 2, 10, 30)));
    }

    @Test
    void rejectsInvalidReduceAndSortByContractsBeforeRuntime() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(ExpressionCompiler.compile(
                        "items := [1]; items.reduce(0, @ -> \"x\")",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile(
                        "items := [1]; items.sortBy(@ -> @, \"up\")",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile(
                        "items := [true]; items.sortBy(@ -> @, \"asc\")",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile(
                        "items := [1]; items.sortBy(@ -> items?.sum(), \"asc\")",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_NULLABLE_ARGUMENT_NOT_ALLOWED"));
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
        ResultExpression expression = ExpressionCompiler.compileOrThrow(
                        "items.sortBy(@ -> trackNumber(@), \"asc\")", environment)
                .asResult();

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

        assertThat(ExpressionCompiler.compileOrThrow("m.map(@ -> @.k)", environment).asResult().compute())
                .isEqualTo(List.of("A", "b"));
        assertThat(ExpressionCompiler.compileOrThrow("m.map(@ -> @.v + 10)", environment).asResult().compute())
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

        assertThat(ExpressionCompiler.compileOrThrow("outer.map(@ -> @[?(@ > 1)].count())", environment).asResult().compute())
                .isEqualTo(List.of(BigDecimal.ONE, BigDecimal.ONE));

        ExpressionEnvironment shallowEnvironment = ExpressionEnvironment.builder()
                .maxCurrentItemDepth(1)
                .build();
        assertThat(ExpressionCompiler.compile("outer := [[1]]; outer.map(@ -> @.map(@ -> @))", shallowEnvironment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED"));
    }

    @Test
    void rejectsInvalidLambdaResults() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        assertThat(ExpressionCompiler.compile("items := [1]; items.all(@ -> @)", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile("items := [1]; items.map(@ -> items?.sum())", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_NULLABLE_ARGUMENT_NOT_ALLOWED"));
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void rejectsEscapedMapEntryLambdaResultsAndEntryEquality() {
        Map<String, BigDecimal> source = new HashMap<>();
        source.put("a", BigDecimal.ONE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), source, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compile("m.map(@ -> [@])", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile("m.any(@ -> @ = @)", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OBJECT_EQUALITY_NOT_SUPPORTED"));
    }

    @Test
    void rejectsLambdaArgumentsForGlobalFunctions() {
        assertThat(ExpressionCompiler.compile("unknown(@ -> @)", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
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
        ResultExpression expression = ExpressionCompiler.compileOrThrow("items.map(@ -> trackNumber(@))", environment).asResult();

        assertThatThrownBy(() -> expression.compute(Map.of(
                        "items", List.of(BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("3")))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("maxMaterializedSize 2");
        assertThat(functions.invocations()).isZero();
    }

    @Test
    void safeMapCallPreservesTheRealReceiverBehaviorWhenNotNull() {
        ExpressionEnvironment environment = ExpressionEnvironment.standard();

        assertThat(ExpressionCompiler.compileOrThrow("items := [1, 2]; items?.map(@ -> @ + 1) ?? []", environment).asResult().compute())
                .isEqualTo(List.of(new BigDecimal("2"), new BigDecimal("3")));
        assertThat(ExpressionCompiler.compileOrThrow("one := [1]; empty := one[1:1]; empty.map(@ -> @ + 1)", environment)
                .asResult().compute()).isEqualTo(List.of());
    }

    @Test
    void materializesNavigatedMapOperationsInCanonicalOrder() {
        Map<String, BigDecimal> source = new HashMap<>();
        source.put("b", new BigDecimal("2"));
        source.put("A", BigDecimal.ONE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), source, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compileOrThrow("m.count()", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("2"));
        assertThat(ExpressionCompiler.compileOrThrow("m.keys()", environment).asResult().compute())
                .isEqualTo(List.of("A", "b"));
        assertThat(ExpressionCompiler.compileOrThrow("m.values()", environment).asResult().compute())
                .isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2")));
        assertThatThrownBy(() -> ((List<Object>) ExpressionCompiler.compileOrThrow("m.keys()", environment).asResult().compute()).add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void readsMapValuesByExplicitStringKeySubscript() {
        Map<String, BigDecimal> source = new HashMap<>();
        source.put("b", new BigDecimal("2"));
        source.put("A", BigDecimal.ONE);
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), source, ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compileOrThrow("m[\"A\"]", environment).asMath().compute())
                .isEqualByComparingTo(BigDecimal.ONE);
        assertThat(ExpressionCompiler.compileOrThrow("m?.[\"b\"] ?? 0", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("2"));
    }

    @Test
    void strictMapStringKeySubscriptFailsOnMissingKeyWhileTheSafeFormYieldsNull() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("A", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThatThrownBy(() -> ExpressionCompiler.compileOrThrow("m[\"missing\"]", environment).asMath().compute())
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(thrown -> assertThat(((ExpressionExecutionException) thrown).diagnostic().code())
                        .isEqualTo("RUNTIME_MAP_KEY_NOT_FOUND"));
        // ADR 0018: the safe link reads the same absence as legitimate and discharges it with `??`.
        assertThat(ExpressionCompiler.compileOrThrow("m?.[\"missing\"] ?? 0", environment).asMath().compute())
                .isEqualByComparingTo(BigDecimal.ZERO);
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

        Object collectionResult = ExpressionCompiler.compileOrThrow("items[*]", environment).asResult().compute();
        assertThat(collectionResult).isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2")));
        assertThatThrownBy(() -> ((List<Object>) collectionResult).add(BigDecimal.TEN))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(ExpressionCompiler.compileOrThrow("m[*]", environment).asResult().compute())
                .isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2")));
        assertThat(ExpressionCompiler.compileOrThrow("object[*]", environment).asResult().compute())
                .isEqualTo(List.of(new BigDecimal("2"), BigDecimal.ONE));
    }

    @Test
    void unorderedObjectWildcardMetadataUsesBinaryMemberOrderAtRuntime() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, Set.of("second", "first"))
                .externalSymbol("object", new WildcardChildProvider(), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compileOrThrow("object[*]", environment).asResult().compute())
                .isEqualTo(List.of(BigDecimal.ONE, new BigDecimal("2")));
    }

    @Test
    void objectWildcardNavigationDoesNotMaskAccessorFailuresOrMaterializationLimits() {
        ExpressionEnvironment failingAccessor = ExpressionEnvironment.builder()
                .registerJavaTypeWildcardChildren(FailingWildcardChildProvider.class, "first")
                .externalSymbol("object", new FailingWildcardChildProvider(), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        assertThatThrownBy(() -> ExpressionCompiler.compileOrThrow("object?.[*] ?? []", failingAccessor).asResult().compute())
                .isInstanceOf(ExpressionExecutionException.class)
                .satisfies(thrown -> assertThat(((ExpressionExecutionException) thrown).diagnostic().code())
                        .isEqualTo("RUNTIME_MEMBER_ACCESS_FAILURE"))
                .hasRootCauseMessage("first failed");

        ExpressionEnvironment limited = ExpressionEnvironment.builder()
                .maxMaterializedSize(1)
                .registerJavaTypeWildcardChildren(WildcardChildProvider.class, "first", "second")
                .externalSymbol("object", new WildcardChildProvider(), ExternalSymbolOverwritePolicy.FIXED)
                .build();
        assertThat(ExpressionCompiler.compile("object[*]", limited))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_MATERIALIZATION_LIMIT_EXCEEDED"));
    }

    @Test
    void executesRegisteredObjectPropertyAndMethodNavigationEndToEnd() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .registerJavaType(PropertyProvider.class)
                .registerJavaTypeMethod(PropertyProvider.class, "scaledAmount", BigDecimal.class)
                .externalSymbol("object", new PropertyProvider(BigDecimal.TEN), ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compileOrThrow("object.amount", environment).asMath().compute())
                .isEqualByComparingTo(BigDecimal.TEN);
        assertThat(ExpressionCompiler.compileOrThrow("object.scaledAmount(2)", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("20"));
    }

    @Test
    void rejectsInvalidNavigatedCollectionOperationCallsBeforeRuntime() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("m", new MapType(ScalarType.NUMBER), Map.of("a", BigDecimal.ONE),
                        ExternalSymbolOverwritePolicy.FIXED)
                .build();

        assertThat(ExpressionCompiler.compile("sum([1])", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_UNKNOWN_FUNCTION"));
        assertThat(ExpressionCompiler.compile("items := [1]; items.keys()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile("m.sum()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile("texts := [\"x\"]; texts.sum()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile("items := [1]; empty := items[1:1]; empty.avg()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile("items := [1]; items.sum(2)", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code()).isEqualTo("SEMANTIC_OPERATOR_TYPE_MISMATCH"));
        assertThat(ExpressionCompiler.compile("items := [true]; items.all()", environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
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

        assertThat(ExpressionCompiler.compileOrThrow("items?.sum() ?? 0", environment).asMath().compute())
                .isEqualByComparingTo(new BigDecimal("3"));
    }

    @Test
    void destructuresCollectionPrefixesAndRejectsInvalidTargets() {
        assertThat(ExpressionCompiler.compileOrThrow("[a, b] := [10, 20, 30]; a + b", ExpressionEnvironment.standard())
                .asMath().compute())
                .isEqualByComparingTo(new BigDecimal("30"));

        assertThat(ExpressionCompiler.compile("[a, a] := [1, 2]; a", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_DUPLICATE_ASSIGNMENT_TARGET"));

        assertThat(ExpressionCompiler.compile("[a, b, c] := [1, 2]; a", ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
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
        MathExpression expression = ExpressionCompiler.compileOrThrow("[a, b] := items; a", environment).asMath();

        assertThat(expression.compute()).isEqualByComparingTo(BigDecimal.ONE);
        assertThatThrownBy(() -> expression.compute(Map.of("items", List.of(BigDecimal.ONE))))
                .isInstanceOf(ExpressionExecutionException.class)
                .hasMessageContaining("destructuring source does not contain enough elements");
    }

    @Test
    void propagatesSliceCardinalityAndDefersFilterCardinality() {
        assertThat(ExpressionCompiler.compile(
                        "items := [1, 2, 3, 4]; [a, b, c] := items[1:3]; a",
                        ExpressionEnvironment.standard()))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_DESTRUCTURING_SIZE_MISMATCH"));

        MathExpression expression = ExpressionCompiler.compileOrThrow(
                        "items := [1, 2]; [a, b] := items[?(@ > 1)]; a",
                        ExpressionEnvironment.standard())
                .asMath();
        assertThatThrownBy(expression::compute)
                .isInstanceOf(ExpressionExecutionException.class)
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

        Object result = ExpressionCompiler.compileOrThrow("outer[?(@[?(@ > 2)] = [3])]", environment).asResult().compute();

        assertThat(result).isEqualTo(List.of(List.of(new BigDecimal("3"))));
    }

    @Test
    void enforcesCurrentItemDepthForNestedFilters() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .maxCurrentItemDepth(1)
                .build();

        assertThat(ExpressionCompiler.compile(
                        "outer := [[1]]; inner := [1]; outer[?(inner[?(@ = 1)] = [1])]",
                        environment))
                .isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure ->
                        assertThat(failure.diagnostics().getFirst().code())
                                .isEqualTo("SEMANTIC_CURRENT_ITEM_DEPTH_EXCEEDED"));
    }

    @Test
    void preservesShortCircuitAndEagerEvaluationContracts() {
        CountingFunctions functions = new CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();

        assertThat(ExpressionCompiler.compileOrThrow("false and markTrue()", environment).asLogical().compute()).isEqualTo(false);
        assertThat(functions.invocations()).isZero();

        assertThat(ExpressionCompiler.compileOrThrow("true or markTrue()", environment).asLogical().compute()).isEqualTo(true);
        assertThat(functions.invocations()).isZero();

        assertThat(ExpressionCompiler.compileOrThrow("false xor markTrue()", environment).asLogical().compute()).isEqualTo(true);
        assertThat(functions.invocations()).isOne();

        assertThat(ExpressionCompiler.compileOrThrow("false nand markTrue()", environment).asLogical().compute()).isEqualTo(true);
        assertThat(functions.invocations()).isEqualTo(2);

        assertThat(ExpressionCompiler.compileOrThrow("true nor markTrue()", environment).asLogical().compute()).isEqualTo(false);
        assertThat(functions.invocations()).isEqualTo(3);

        assertThat(ExpressionCompiler.compileOrThrow("1 ?? markNumber()", environment).asMath().compute()).isEqualByComparingTo(new BigDecimal("1"));
        assertThat(functions.invocations()).isEqualTo(3);

        assertThat(ExpressionCompiler.compileOrThrow("0 between 1 and markNumber()", environment).asLogical().compute()).isEqualTo(false);
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

        int invocations() {
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

    public record PropertyProvider(BigDecimal amount) {

        public BigDecimal scaledAmount(BigDecimal factor) {
            return amount.multiply(factor);
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
}
