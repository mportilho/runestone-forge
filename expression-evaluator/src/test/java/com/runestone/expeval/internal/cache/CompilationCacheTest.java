package com.runestone.expeval.internal.cache;

import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.api.DiagnosticCategory;
import com.runestone.expeval.api.ExpressionCompilationResult;
import com.runestone.expeval.api.ExpressionDiagnostic;
import com.runestone.expeval.api.ExpressionEnvironment;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Issue #134's cache-loader contract in isolation from {@code ExpressionEngine}: a hit returns the
 * resident {@link ExpressionCompilationResult}, and an unexpected exception from the compiler escapes
 * without installing an entry, so a later call for the same key can retry.
 */
class CompilationCacheTest {

    @Test
    void anUnexpectedCompilerFailureInstallsNoEntryAndALaterCallCanRetry() {
        AtomicInteger calls = new AtomicInteger();
        CompilationCache cache = new CompilationCache(CacheConfig.defaults(), (source, environment) -> {
            if (calls.incrementAndGet() == 1) {
                throw new IllegalStateException("transient bug");
            }
            return compilation(failure());
        });
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        assertThatThrownBy(() -> cache.get("source", environment)).isInstanceOf(IllegalStateException.class);
        ExpressionCompilationResult retried = cache.get("source", environment);

        assertThat(retried).isInstanceOf(ExpressionCompilationResult.Failure.class);
        assertThat(calls.get()).isEqualTo(2);
    }

    @Test
    void theCacheKeyRetainsTheExactSourceReferenceItWasBuiltFrom() {
        String source = new String("1 + 2".toCharArray());
        CompilationCacheKey key = new CompilationCacheKey(source, "environment-id");

        assertThat(key.source()).as("the key intentionally retains the caller's exact source reference")
                .isSameAs(source);
    }

    @Test
    void aHitReturnsTheResidentResultWithoutCallingTheCompilerAgain() {
        AtomicInteger calls = new AtomicInteger();
        CompilationCache cache = new CompilationCache(CacheConfig.defaults(), (source, environment) -> {
            calls.incrementAndGet();
            return compilation(failure());
        });
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();

        ExpressionCompilationResult first = cache.get("source", environment);
        ExpressionCompilationResult second = cache.get("source", environment);

        assertThat(second).isSameAs(first);
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void invalidatingDuringCompilationPreventsTheDeliveredGenerationFromBecomingResident() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CompilationCache cache = new CompilationCache(CacheConfig.defaults(), (source, environment) -> {
            calls.incrementAndGet();
            entered.countDown();
            await(release);
            return compilation(failure());
        });
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ExpressionCompilationResult> firstFuture = executor.submit(() -> cache.get("source", environment));
            entered.await();
            Future<?> invalidation = executor.submit(() -> cache.invalidate("source", environment));
            release.countDown();
            invalidation.get();
            ExpressionCompilationResult first = firstFuture.get();

            assertThat(cache.get("source", environment)).isNotSameAs(first);
            assertThat(calls.get()).isEqualTo(2);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void aCallerArrivingAfterInvalidationWaitsForANewGeneration() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        CountDownLatch firstCompilationEntered = new CountDownLatch(1);
        CountDownLatch releaseFirstCompilation = new CountDownLatch(1);
        CompilationCache cache = new CompilationCache(CacheConfig.defaults(), (source, environment) -> {
            int call = calls.incrementAndGet();
            if (call == 1) {
                firstCompilationEntered.countDown();
                await(releaseFirstCompilation);
            }
            return compilation(failure("generation-" + call));
        });
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            Future<ExpressionCompilationResult> firstFuture = executor.submit(() -> cache.get("source", environment));
            firstCompilationEntered.await();
            Future<?> invalidation = executor.submit(() -> cache.invalidate("source", environment));
            awaitCondition(() -> cache.isInFlightInvalidated("source", environment));

            Future<ExpressionCompilationResult> laterFuture =
                    executor.submit(() -> cache.get("source", environment));
            releaseFirstCompilation.countDown();

            ExpressionCompilationResult first = firstFuture.get();
            ExpressionCompilationResult later = laterFuture.get();
            invalidation.get();
            assertThat(later).isNotSameAs(first);
            assertThat(cache.get("source", environment)).isSameAs(later);
            assertThat(calls.get()).isEqualTo(2);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void unrelatedKeysCollidingInAFlightStripeCompileConcurrently() throws Exception {
        CountDownLatch compilersEntered = new CountDownLatch(2);
        CountDownLatch releaseCompilers = new CountDownLatch(1);
        CompilationCache cache = new CompilationCache(CacheConfig.defaults(), (source, environment) -> {
            compilersEntered.countDown();
            await(releaseCompilers);
            return compilation(failure(source));
        });
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        String firstSource = "source-0";
        int stripe = stripe(firstSource, environment);
        String secondSource = java.util.stream.IntStream.range(1, 10_000)
                .mapToObj(index -> "source-" + index)
                .filter(source -> stripe(source, environment) == stripe)
                .findFirst()
                .orElseThrow();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<ExpressionCompilationResult> first = executor.submit(() -> cache.get(firstSource, environment));
            Future<ExpressionCompilationResult> second = executor.submit(() -> cache.get(secondSource, environment));

            assertThat(compilersEntered.await(10, TimeUnit.SECONDS))
                    .as("a stripe collision must not serialize compilation outside the short stripe lock")
                    .isTrue();
            releaseCompilers.countDown();

            assertThat(first.get()).isNotSameAs(second.get());
        } finally {
            releaseCompilers.countDown();
            executor.shutdownNow();
        }
    }

    private static int stripe(String source, ExpressionEnvironment environment) {
        return new CompilationCacheKey(source, environment.environmentId()).hashCode() & 63;
    }

    private static CompilationCache.CachedCompilation compilation(ExpressionCompilationResult result) {
        return new CompilationCache.CachedCompilation(result, 1_024);
    }

    private static ExpressionCompilationResult.Failure failure() {
        return failure("test failure");
    }

    private static ExpressionCompilationResult.Failure failure(String message) {
        return new ExpressionCompilationResult.Failure(List.of(ExpressionDiagnostic.error(
                DiagnosticCategory.SEMANTIC, "TEST_CODE", message, null)));
    }

    private static void awaitCondition(BooleanSupplier condition) {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertThat(condition.getAsBoolean()).isTrue();
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
