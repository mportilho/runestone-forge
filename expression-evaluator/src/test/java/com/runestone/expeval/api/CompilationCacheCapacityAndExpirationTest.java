package com.runestone.expeval.api;

import com.runestone.expeval.internal.cache.CompilationCache;
import com.runestone.expeval.internal.cache.MonotonicTicker;
import com.runestone.expeval.internal.runtime.RuntimeServices;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #136's capacity and expiration policies, extended by issue #171's retained-weight admission,
 * sit on top of the single-flight contract issue #135 already proved. Eviction, expiration, and rejected
 * admission must let a new generation be compiled without invalidating a {@link CompiledExpression} already
 * delivered. Caffeine may reject an incoming entry instead of evicting a resident one, so tests never assert
 * which specific entry is gone; a pigeonhole count over a batch far larger than the configured budget is the
 * only assertion that holds under any admission decision.
 */
class CompilationCacheCapacityAndExpirationTest {

    @Test
    void capacityEvictsByEntryCountAcrossABatchAndPermitsANewGenerationWhileThePreviousStaysExecutable() {
        int capacity = 4;
        int sourceCount = 40;
        CompilationCache cache = realPipelineCache(CacheConfig.builder().maximumEntries(capacity).build());
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        List<String> sources = IntStream.range(0, sourceCount)
                .mapToObj(index -> index + " + " + index)
                .collect(Collectors.toList());

        List<ExpressionCompilationResult> firstPass = sources.stream()
                .map(source -> cache.get(source, environment))
                .collect(Collectors.toList());
        List<ExpressionCompilationResult> secondPass = sources.stream()
                .map(source -> cache.get(source, environment))
                .collect(Collectors.toList());

        long changedGenerations = IntStream.range(0, sourceCount)
                .filter(index -> secondPass.get(index) != firstPass.get(index))
                .count();
        assertThat(changedGenerations)
                .as("at least sourceCount - capacity entries must have been evicted at least once")
                .isGreaterThanOrEqualTo(sourceCount - capacity);

        int evictedIndex = IntStream.range(0, sourceCount)
                .filter(index -> secondPass.get(index) != firstPass.get(index))
                .findFirst()
                .orElseThrow();
        ExpressionCompilationResult.Success oldGeneration =
                (ExpressionCompilationResult.Success) firstPass.get(evictedIndex);
        BigDecimal expected = BigDecimal.valueOf(evictedIndex).add(BigDecimal.valueOf(evictedIndex));

        assertThat((BigDecimal) oldGeneration.compiledExpression().asResult().compute())
                .as("a Visao de Expressao from an evicted generation keeps executing correctly")
                .isEqualByComparingTo(expected);
        assertThat(secondPass.get(evictedIndex))
                .as("a later call for the same key creates a new generation instead of failing")
                .isInstanceOf(ExpressionCompilationResult.Success.class);
    }

    @Test
    void configuredRetainedWeightBudgetKeepsOrdinaryExpressionsOfDifferentPlanSizesResident() {
        int capacity = 5;
        CompilationCache cache = realPipelineCache(CacheConfig.builder()
                .maximumEntries(capacity)
                .maximumRetainedWeight(5L * 1024 * 1024)
                .build());
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        List<String> sources = List.of(1, 10, 50, 150, 500).stream()
                .map(termCount -> IntStream.range(0, termCount).mapToObj(ignored -> "1").collect(Collectors.joining(" + ")))
                .collect(Collectors.toList());

        List<ExpressionCompilationResult> firstPass = sources.stream()
                .map(source -> cache.get(source, environment))
                .collect(Collectors.toList());
        for (int repeat = 0; repeat < 10; repeat++) {
            for (int index = 0; index < sources.size(); index++) {
                assertThat(cache.get(sources.get(index), environment))
                        .as("the configured retained-weight budget accommodates this ordinary expression with %d nodes",
                                index)
                        .isSameAs(firstPass.get(index));
            }
        }
    }

    @Test
    void anIndividuallyOverweightSuccessIsDeliveredButNeverResidentInEveryTrustMode() {
        CacheConfig config = CacheConfig.builder()
                .maximumEntries(1)
                .maximumRetainedWeight(1)
                .build();
        String source = "1 + 2";

        for (ExpressionTrustMode trustMode : ExpressionTrustMode.values()) {
            ExpressionEngine engine = ExpressionEngine.builder().cacheConfig(config).build();
            ExpressionEnvironment environment = ExpressionEnvironment.builder().trustMode(trustMode).build();

            ExpressionCompilationResult.Success first =
                    (ExpressionCompilationResult.Success) engine.compile(source, environment);
            ExpressionCompilationResult.Success second =
                    (ExpressionCompilationResult.Success) engine.compile(source, environment);

            assertThat(second)
                    .as("%s still delivers successful compilation even when it cannot be resident", trustMode)
                    .isNotSameAs(first);
            assertThat((BigDecimal) first.compiledExpression().asResult().compute())
                    .as("an already-delivered generation remains valid after rejected admission in %s", trustMode)
                    .isEqualByComparingTo(BigDecimal.valueOf(3));
        }
    }

    @Test
    void entryCountLimitRemainsActiveInEveryTrustMode() {
        int sourceCount = 12;
        CacheConfig config = CacheConfig.builder()
                .maximumEntries(1)
                .maximumRetainedWeight(1024L * 1024)
                .build();
        List<String> sources = IntStream.range(0, sourceCount)
                .mapToObj(index -> index + " + " + index)
                .toList();

        for (ExpressionTrustMode trustMode : ExpressionTrustMode.values()) {
            ExpressionEngine engine = ExpressionEngine.builder().cacheConfig(config).build();
            ExpressionEnvironment environment = ExpressionEnvironment.builder().trustMode(trustMode).build();
            List<ExpressionCompilationResult> firstPass = sources.stream()
                    .map(source -> engine.compile(source, environment))
                    .toList();
            List<ExpressionCompilationResult> secondPass = sources.stream()
                    .map(source -> engine.compile(source, environment))
                    .toList();

            long changedGenerations = IntStream.range(0, sourceCount)
                    .filter(index -> firstPass.get(index) != secondPass.get(index))
                    .count();
            assertThat(changedGenerations)
                    .as("maximumEntries remains a one-entry bound in %s", trustMode)
                    .isGreaterThanOrEqualTo(sourceCount - 1);
        }
    }

    @Test
    void retainedWeightLimitRemainsActiveInEveryTrustMode() {
        List<String> sources = IntStream.range(0, 12)
                .mapToObj(index -> index + " + " + index)
                .toList();

        for (ExpressionTrustMode trustMode : ExpressionTrustMode.values()) {
            ExpressionEnvironment environment = ExpressionEnvironment.builder().trustMode(trustMode).build();
            ExpressionEngine sizingEngine = ExpressionEngine.builder().build();
            int lightestEntryWeight = sources.stream()
                    .mapToInt(source -> CompilationRetainedWeight.estimate(source, sizingEngine.compile(source, environment)))
                    .min()
                    .orElseThrow();
            CacheConfig config = CacheConfig.builder()
                    .maximumEntries(100)
                    .maximumRetainedWeight(2L * lightestEntryWeight)
                    .build();
            ExpressionEngine engine = ExpressionEngine.builder().cacheConfig(config).build();
            List<ExpressionCompilationResult> firstPass = sources.stream()
                    .map(source -> engine.compile(source, environment))
                    .toList();
            List<ExpressionCompilationResult> secondPass = sources.stream()
                    .map(source -> engine.compile(source, environment))
                    .toList();

            long changedGenerations = IntStream.range(0, sources.size())
                    .filter(index -> firstPass.get(index) != secondPass.get(index))
                    .count();
            assertThat(changedGenerations)
                    .as("two entry weights admit at most two of twelve sources in %s", trustMode)
                    .isGreaterThanOrEqualTo(sources.size() - 2);
        }
    }

    @Test
    void expirationCountsFromTheLastAccessNotFromTheOriginalWriteAndPermitsANewGenerationWhileThePreviousStaysExecutable() {
        FakeTicker ticker = new FakeTicker();
        CacheConfig config = CacheConfig.builder().expireAfterAccess(Duration.ofMinutes(1)).build();
        CompilationCache cache = realPipelineCache(config, ticker);
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult.Success firstGeneration =
                (ExpressionCompilationResult.Success) cache.get("1 + 2", environment);
        ticker.advance(Duration.ofSeconds(50));
        ExpressionCompilationResult.Success secondAccess =
                (ExpressionCompilationResult.Success) cache.get("1 + 2", environment);
        ticker.advance(Duration.ofSeconds(40));
        ExpressionCompilationResult.Success thirdAccess =
                (ExpressionCompilationResult.Success) cache.get("1 + 2", environment);

        assertThat(secondAccess).isSameAs(firstGeneration);
        assertThat(thirdAccess)
                .as("90s have passed since the original write (past the 60s window) but only 40s since the last "
                        + "access, so a write-based policy would have expired this and an access-based one would not")
                .isSameAs(firstGeneration);

        ticker.advance(Duration.ofSeconds(65));
        ExpressionCompilationResult.Success afterLongIdle =
                (ExpressionCompilationResult.Success) cache.get("1 + 2", environment);

        assertThat(afterLongIdle)
                .as("65s idle since the last access, past the 60s window, forces a new generation for the same key")
                .isNotSameAs(firstGeneration);
        assertThat((BigDecimal) firstGeneration.compiledExpression().asResult().compute())
                .as("a Visao de Expressao from an expired generation keeps executing correctly")
                .isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat((BigDecimal) afterLongIdle.compiledExpression().asResult().compute())
                .isEqualByComparingTo(BigDecimal.valueOf(3));
    }

    @Test
    void expirationDisabledByDefaultNeverEvictsRegardlessOfHowFarTheTickerAdvances() {
        FakeTicker ticker = new FakeTicker();
        CompilationCache cache = realPipelineCache(CacheConfig.defaults(), ticker);
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult first = cache.get("1 + 2", environment);
        ticker.advance(Duration.ofDays(365));

        assertThat(cache.get("1 + 2", environment))
                .as("with no expireAfterAccess configured, no amount of idle time expires an entry")
                .isSameAs(first);
    }

    @Test
    void theExpirationTickerIsIndependentFromTheSemanticClockUsedByCompiledExpressions() {
        FakeTicker ticker = new FakeTicker();
        CacheConfig config = CacheConfig.builder().expireAfterAccess(Duration.ofMinutes(1)).build();
        Clock fixedSemanticClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
        RuntimeServices runtimeServices = RuntimeServices.withClock(fixedSemanticClock);
        CompilationCache cache = new CompilationCache(
                config, (source, environment) -> weighted(
                        source, CompilationPipeline.compile(source, environment, runtimeServices)), ticker);
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult.Success firstGeneration =
                (ExpressionCompilationResult.Success) cache.get("currDateTime", environment);
        ticker.advance(Duration.ofMinutes(5));
        ExpressionCompilationResult.Success secondGeneration =
                (ExpressionCompilationResult.Success) cache.get("currDateTime", environment);

        assertThat(secondGeneration)
                .as("advancing the expiration ticker forces a new generation")
                .isNotSameAs(firstGeneration);
        assertThat(secondGeneration.compiledExpression().asResult().compute())
                .as("the new generation still observes the same fixed semantic Clock: advancing the internal "
                        + "expiration ticker never moves the language's current-time value")
                .isEqualTo(firstGeneration.compiledExpression().asResult().compute());
    }

    private static CompilationCache realPipelineCache(CacheConfig config) {
        return realPipelineCache(config, MonotonicTicker.SYSTEM);
    }

    private static CompilationCache realPipelineCache(CacheConfig config, MonotonicTicker ticker) {
        RuntimeServices runtimeServices = RuntimeServices.systemDefault();
        return new CompilationCache(
                config, (source, environment) -> weighted(
                        source, CompilationPipeline.compile(source, environment, runtimeServices)), ticker);
    }

    private static CompilationCache.CachedCompilation weighted(String source, ExpressionCompilationResult result) {
        return new CompilationCache.CachedCompilation(result, CompilationRetainedWeight.estimate(source, result));
    }

    /** No wall-clock or {@code sleep} dependency: time only moves when a test calls {@link #advance}. */
    private static final class FakeTicker implements MonotonicTicker {

        private final AtomicLong nanos = new AtomicLong();

        @Override
        public long read() {
            return nanos.get();
        }

        void advance(Duration duration) {
            nanos.addAndGet(duration.toNanos());
        }
    }
}
