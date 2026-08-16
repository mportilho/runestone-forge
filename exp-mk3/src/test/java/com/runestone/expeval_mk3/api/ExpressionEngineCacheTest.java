package com.runestone.expeval_mk3.api;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Issue #134: {@link ExpressionEngine} as the sole public compilation boundary and the identity,
 * isolation, and validation contract of its per-engine cache and {@link CacheConfig}, plus this suite's
 * addition of issue #135's single-threaded incompatible-view proof. Genuine concurrent single-flight has
 * its own dedicated proof in {@link SingleFlightCompilationTest} (issue #135); capacity and expiration
 * have their own dedicated proof (issue #136). This suite covers what a single-threaded caller can
 * observe about sharing and isolation.
 */
class ExpressionEngineCacheTest {

    @Test
    void defaultEngineIsTheSameLazySingletonAcrossCallsAndThreads() throws Exception {
        int callCount = 16;
        ExecutorService executor = Executors.newFixedThreadPool(callCount);
        try {
            var calls = IntStream.range(0, callCount)
                    .mapToObj(value -> (Callable<ExpressionEngine>) ExpressionEngine::defaultEngine)
                    .collect(Collectors.toList());
            var futures = calls.stream().map(executor::submit).collect(Collectors.toList());

            ExpressionEngine first = ExpressionEngine.defaultEngine();
            for (Future<ExpressionEngine> future : futures) {
                assertThat(future.get()).isSameAs(first);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void builderCreatesADistinctIsolatedEngineEveryTimeUsingDefaultsWhenUnconfigured() {
        ExpressionEngine first = ExpressionEngine.builder().build();
        ExpressionEngine second = ExpressionEngine.builder().build();

        assertThat(first).isNotSameAs(second);
        assertThat(first).isNotSameAs(ExpressionEngine.defaultEngine());
    }

    @Test
    void aCompileHitReturnsTheSameSuccessInstanceAsTheResidentGeneration() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult first = engine.compile("1 + 2", environment);
        ExpressionCompilationResult second = engine.compile(new String("1 + 2".toCharArray()), environment);

        assertThat(first).isInstanceOf(ExpressionCompilationResult.Success.class);
        assertThat(second).isSameAs(first);
    }

    @Test
    void aCompileHitReturnsTheSameFailureInstanceAsTheResidentGeneration() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult first = engine.compile("1 +", environment);
        ExpressionCompilationResult second = engine.compile(new String("1 +".toCharArray()), environment);

        assertThat(first).isInstanceOf(ExpressionCompilationResult.Failure.class);
        assertThat(second).isSameAs(first);
    }

    @Test
    void compileOrThrowHitReturnsTheSameCompiledExpressionOnSuccess() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        CompiledExpression first = engine.compileOrThrow("1 + 2", environment);
        CompiledExpression second = engine.compileOrThrow("1 + 2", environment);

        assertThat(second).isSameAs(first);
    }

    @Test
    void compileOrThrowCreatesANewExceptionInstanceOnEveryFailureHit() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        engine.compile("1 +", environment);

        ExpressionCompilationException first = catchCompilationException(() -> engine.compileOrThrow("1 +", environment));
        ExpressionCompilationException second = catchCompilationException(() -> engine.compileOrThrow("1 +", environment));

        assertThat(second).isNotSameAs(first);
        assertThat(second.diagnostics()).isEqualTo(first.diagnostics());
        // Together with aCompileHitReturnsTheSameFailureInstanceAsTheResidentGeneration (same Failure resident on
        // every hit), the fresh stack trace below shows the cache never holds a Throwable as a value: only the
        // immutable diagnostics are resident, and compileOrThrow rebuilds the exception from them on every call.
        assertThat(second.getStackTrace()).as("each failure hit captures its own fresh stack trace")
                .isNotEqualTo(first.getStackTrace());
    }

    @Test
    void anIncompatibleViewFailsWithoutRecompilingOrExecutingProviders() {
        ExpressionEngineTest.CountingFunctions functions = new ExpressionEngineTest.CountingFunctions();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(functions, FunctionPurity.IMPURE)
                .build();
        ExpressionEngine engine = ExpressionEngine.builder().build();
        String source = "x := bump(1);";
        CompiledExpression assignmentsOnly = engine.compileOrThrow(source, environment);

        assertRejectsWithNoResultExpression(assignmentsOnly::asResult);
        assertRejectsWithNoResultExpression(assignmentsOnly::asMath);
        assertRejectsWithNoResultExpression(assignmentsOnly::asLogical);

        assertThat(functions.invocations())
                .as("view rejection is decided from the plan's static shape and never runs the assignment's function")
                .isZero();
        assertThat(engine.compileOrThrow(source, environment))
                .as("repeated view rejections never trigger recompilation")
                .isSameAs(assignmentsOnly);
    }

    @Test
    void aTextuallyDifferentSourceProducesADistinctEntry() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult first = engine.compile("1 + 2", environment);
        ExpressionCompilationResult second = engine.compile("1 +  2", environment);

        assertThat(second).isNotSameAs(first);
    }

    @Test
    void theSameSourceWithADifferentEnvironmentInstanceProducesADistinctEntry() {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment firstEnvironment = ExpressionEnvironment.builder().build();
        ExpressionEnvironment secondEnvironment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult first = engine.compile("1 + 2", firstEnvironment);
        ExpressionCompilationResult second = engine.compile("1 + 2", secondEnvironment);

        assertThat(second).isNotSameAs(first);
    }

    @Test
    void theSameSourceAndEnvironmentInDistinctEnginesShareNoCacheEntry() {
        ExpressionEngine firstEngine = ExpressionEngine.builder().build();
        ExpressionEngine secondEngine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult fromFirst = firstEngine.compile("1 + 2", environment);
        ExpressionCompilationResult fromSecond = secondEngine.compile("1 + 2", environment);

        assertThat(fromSecond).isNotSameAs(fromFirst);
    }

    @Test
    void isolatedEnginesOwnDistinctRuntimeServicesAndNeverShareTheirClock() {
        Clock firstClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
        Clock secondClock = Clock.fixed(Instant.parse("2030-06-15T10:00:00Z"), ZoneOffset.UTC);
        ExpressionEngine firstEngine = ExpressionEngine.builder().clock(firstClock).build();
        ExpressionEngine secondEngine = ExpressionEngine.builder().clock(secondClock).build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().zoneId(ZoneOffset.UTC).build();

        LocalDateTime fromFirst = (LocalDateTime) firstEngine.compileOrThrow("currDateTime", environment)
                .asResult()
                .compute();
        LocalDateTime fromSecond = (LocalDateTime) secondEngine.compileOrThrow("currDateTime", environment)
                .asResult()
                .compute();

        assertThat(fromFirst).isEqualTo(LocalDateTime.of(2024, 1, 1, 0, 0, 0));
        assertThat(fromSecond).isEqualTo(LocalDateTime.of(2030, 6, 15, 10, 0, 0));
    }

    @Test
    void allCompatibleViewsIncludingAssignmentsShareTheSameCompiledExpressionAcrossHits() {
        // A root Math expression rejects asLogical() (and vice versa); ConcurrentSharedPlanExecutionTest
        // covers asResult/asLogical/asAssignments on a logical root, so this covers the Math-rooted
        // combination to complete the explicit shared-plan proof across all four public views.
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        String source = "x := 1 + 2; x";

        CompiledExpression first = engine.compileOrThrow(source, environment);
        CompiledExpression second = engine.compileOrThrow(source, environment);

        assertThat(second).isSameAs(first);
        assertThat(second.asResult().compute()).isEqualTo(new java.math.BigDecimal("3"));
        assertThat(second.asMath().compute()).isEqualByComparingTo(new java.math.BigDecimal("3"));
        assertThat(second.asAssignments().compute()).isEqualTo(Map.of("x", new java.math.BigDecimal("3")));
    }

    @Test
    void cacheConfigDefaultsAreOneThousandTwentyFourEntriesWithNoExpiration() {
        CacheConfig defaults = CacheConfig.defaults();

        assertThat(defaults.maximumEntries()).isEqualTo(1024);
        assertThat(defaults.hasExpireAfterAccess()).isFalse();
        assertThatThrownBy(defaults::expireAfterAccess).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cacheConfigBuilderRejectsZeroOrNegativeCapacity() {
        assertThatThrownBy(() -> CacheConfig.builder().maximumEntries(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CacheConfig.builder().maximumEntries(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cacheConfigBuilderRejectsNullZeroOrNegativeExpiration() {
        assertThatThrownBy(() -> CacheConfig.builder().expireAfterAccess(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> CacheConfig.builder().expireAfterAccess(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CacheConfig.builder().expireAfterAccess(Duration.ofSeconds(-1)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cacheConfigBuilderAcceptsAPositiveCapacityAndExpiration() {
        CacheConfig config = CacheConfig.builder()
                .maximumEntries(16)
                .expireAfterAccess(Duration.ofMinutes(5))
                .build();

        assertThat(config.maximumEntries()).isEqualTo(16);
        assertThat(config.hasExpireAfterAccess()).isTrue();
        assertThat(config.expireAfterAccess()).isEqualTo(Duration.ofMinutes(5));
    }

    private static void assertRejectsWithNoResultExpression(ThrowingCallable view) {
        assertThatThrownBy(view).isInstanceOfSatisfying(ExpressionViewException.class, exception ->
                assertThat(exception.reason()).isEqualTo(ExpressionViewException.Reason.NO_RESULT_EXPRESSION));
    }

    private static ExpressionCompilationException catchCompilationException(Runnable runnable) {
        try {
            runnable.run();
        } catch (ExpressionCompilationException exception) {
            return exception;
        }
        throw new AssertionError("expected an ExpressionCompilationException");
    }
}
