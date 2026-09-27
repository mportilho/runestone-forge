package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.cache.CompilationCache;
import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationRetainedWeightTest {

    @Test
    void sourceAndDiagnosticPayloadIncreaseTheConservativeWeight() {
        ExpressionCompilationResult shortFailure = failure("brief");
        ExpressionCompilationResult longFailure = failure("diagnostic payload that is deliberately much longer than brief");

        int shortWeight = CompilationRetainedWeight.estimate("1 +", shortFailure);
        int longerSourceWeight = CompilationRetainedWeight.estimate("1 + 2 + 3 + 4 + 5", shortFailure);
        int longerDiagnosticWeight = CompilationRetainedWeight.estimate("1 +", longFailure);

        assertThat(longerSourceWeight).isGreaterThan(shortWeight);
        assertThat(longerDiagnosticWeight).isGreaterThan(shortWeight);
    }

    @Test
    void planWeightIncludesDownloadedPlanShapeAndFoldedConstantPayload() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        ExpressionCompilationResult.Success scalar =
                (ExpressionCompilationResult.Success) engine.compile("\"a\"", environment);
        String foldedSource = IntStream.range(0, 8)
                .mapToObj(ignored -> "\"a\"")
                .collect(java.util.stream.Collectors.joining(" || "));
        ExpressionCompilationResult.Success folded =
                (ExpressionCompilationResult.Success) engine.compile(foldedSource, environment);

        assertThat(folded.compiledExpression().estimatedRetainedPlanWeight())
                .isGreaterThan(scalar.compiledExpression().estimatedRetainedPlanWeight());
    }

    @Test
    void preparedRegexPayloadScalesWithItsSourceControlledPattern() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("text", ScalarType.STRING, "a", ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        ExpressionCompilationResult.Success shortPattern =
                (ExpressionCompilationResult.Success) engine.compile("text =~ \"a+\"", environment);
        ExpressionCompilationResult.Success longPattern = (ExpressionCompilationResult.Success) engine.compile(
                "text =~ \"" + "a".repeat(512) + "\"", environment);

        assertThat(longPattern.compiledExpression().estimatedRetainedPlanWeight())
                .isGreaterThan(shortPattern.compiledExpression().estimatedRetainedPlanWeight());
    }

    @Test
    void staticCalculationProvenanceContributesItsRetainedMetadata() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(new PureMarkFunction(), FunctionPurity.PURE)
                .build();
        ExpressionCompilationResult.Success literal =
                (ExpressionCompilationResult.Success) engine.compile("1", environment);
        ExpressionCompilationResult.Success foldedCalculation =
                (ExpressionCompilationResult.Success) engine.compile("mark(1)", environment);

        assertThat(foldedCalculation.compiledExpression().asMath().computeWithMemory().memory().calculationCount())
                .isOne();
        assertThat(foldedCalculation.compiledExpression().estimatedRetainedPlanWeight())
                .isGreaterThan(literal.compiledExpression().estimatedRetainedPlanWeight());
    }

    @Test
    void planWeightIncludesPlanOwnedEnvironmentIndexesButExcludesExternalDefaultPayload() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionCompilationResult.Success smallEnvironment =
                (ExpressionCompilationResult.Success) engine.compile("1", environmentWithSymbols(1));
        ExpressionCompilationResult.Success largeEnvironment =
                (ExpressionCompilationResult.Success) engine.compile("1", environmentWithSymbols(128));
        ExpressionCompilationResult.Success shortDefault = (ExpressionCompilationResult.Success) engine.compile(
                "external", externalValueEnvironment("x"));
        ExpressionCompilationResult.Success longDefault = (ExpressionCompilationResult.Success) engine.compile(
                "external", externalValueEnvironment("x".repeat(4_096)));

        assertThat(largeEnvironment.compiledExpression().estimatedRetainedPlanWeight())
                .isGreaterThan(smallEnvironment.compiledExpression().estimatedRetainedPlanWeight());
        assertThat(longDefault.compiledExpression().estimatedRetainedPlanWeight())
                .isEqualTo(shortDefault.compiledExpression().estimatedRetainedPlanWeight());
    }

    @Test
    void planWeightExcludesAnEnvironmentValueRetainedByAParentFold() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionCompilationResult.Success shortValue = (ExpressionCompilationResult.Success) engine.compile(
                "identity(external)", fixedExternalValueEnvironmentWithIdentityFunction("x"));
        ExpressionCompilationResult.Success longValue = (ExpressionCompilationResult.Success) engine.compile(
                "identity(external)", fixedExternalValueEnvironmentWithIdentityFunction("x".repeat(4_096)));

        assertThat(longValue.compiledExpression().estimatedRetainedPlanWeight())
                .isEqualTo(shortValue.compiledExpression().estimatedRetainedPlanWeight());
    }

    @Test
    void retainedWeightEvictsAcrossABatchBeforeTheIndependentEntryLimitIsReached() {
        AtomicInteger compilations = new AtomicInteger();
        ExpressionCompilationResult result = failure("payload");
        List<String> sources = IntStream.range(0, 20)
                .mapToObj(index -> "source-" + index)
                .toList();
        int lightestEntryWeight = sources.stream()
                .mapToInt(source -> CompilationRetainedWeight.estimate(source, result))
                .min()
                .orElseThrow();
        CompilationCache cache = new CompilationCache(
                CacheConfig.builder()
                        .maximumEntries(100)
                        .maximumRetainedWeight(2L * lightestEntryWeight)
                .build(),
                (source, environment) -> {
                    compilations.incrementAndGet();
                    ExpressionCompilationResult.Failure failure = failure("payload");
                    return new CompilationCache.CachedCompilation(
                            failure, CompilationRetainedWeight.estimate(source, failure));
                });
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        List<ExpressionCompilationResult> firstPass = sources.stream()
                .map(source -> cache.get(source, environment))
                .toList();
        List<ExpressionCompilationResult> secondPass = sources.stream()
                .map(source -> cache.get(source, environment))
                .toList();

        long replacedGenerations = IntStream.range(0, sources.size())
                .filter(index -> firstPass.get(index) != secondPass.get(index))
                .count();
        assertThat(replacedGenerations)
                .as("the weight budget, not the 100-entry limit, must reject at least eighteen of twenty entries")
                .isGreaterThanOrEqualTo(18);
        assertThat(compilations.get()).isGreaterThanOrEqualTo(38);
    }

    @Test
    void calibrationUsesJolLayoutsAsConservativeFloorsWithoutPresentingUnitsAsExactHeapBytes() {
        assertThat(CompilationRetainedWeight.ENTRY_UNITS)
                .isGreaterThanOrEqualTo(Math.toIntExact(ClassLayout.parseClass(CompiledExpression.class).instanceSize()));
        assertThat(CompilationRetainedWeight.DIAGNOSTIC_UNITS)
                .isGreaterThanOrEqualTo(Math.toIntExact(ClassLayout.parseClass(ExpressionDiagnostic.class).instanceSize()));
    }

    private static ExpressionCompilationResult.Failure failure(String message) {
        return new ExpressionCompilationResult.Failure(List.of(ExpressionDiagnostic.error(
                DiagnosticCategory.SEMANTIC, "TEST_CODE", message, null)));
    }

    private static ExpressionEnvironment environmentWithSymbols(int count) {
        ExpressionEnvironment.Builder builder = ExpressionEnvironment.builder();
        IntStream.range(0, count).forEach(index -> builder.externalSymbol(
                "value" + index, ScalarType.STRING, "", ExternalSymbolOverwritePolicy.OVERRIDABLE));
        return builder.build();
    }

    private static ExpressionEnvironment externalValueEnvironment(String defaultValue) {
        return ExpressionEnvironment.builder()
                .externalSymbol("external", ScalarType.STRING, defaultValue, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
    }

    private static ExpressionEnvironment fixedExternalValueEnvironmentWithIdentityFunction(String defaultValue) {
        return ExpressionEnvironment.builder()
                .externalSymbol("external", ScalarType.STRING, defaultValue, ExternalSymbolOverwritePolicy.FIXED)
                .functionsFrom(new IdentityStringFunction(), FunctionPurity.FOLDABLE)
                .build();
    }

    public static final class PureMarkFunction {

        public java.math.BigDecimal mark(java.math.BigDecimal value) {
            return value;
        }
    }

    public static final class IdentityStringFunction {

        public String identity(String value) {
            return value;
        }
    }
}
