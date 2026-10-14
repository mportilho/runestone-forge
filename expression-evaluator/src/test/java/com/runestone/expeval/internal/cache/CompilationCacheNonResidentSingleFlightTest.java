package com.runestone.expeval.internal.cache;

import com.runestone.expeval.api.CacheConfig;
import com.runestone.expeval.api.DiagnosticCategory;
import com.runestone.expeval.api.ExpressionCompilationResult;
import com.runestone.expeval.api.ExpressionDiagnostic;
import com.runestone.expeval.api.ExpressionEnvironment;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class CompilationCacheNonResidentSingleFlightTest {

    @Test
    void callersJoinedToAnOverweightMissReceiveTheSameDeliveredGeneration() throws Exception {
        int callerCount = 32;
        AtomicInteger compilerCalls = new AtomicInteger();
        CountDownLatch compilerEntered = new CountDownLatch(1);
        CountDownLatch releaseCompiler = new CountDownLatch(1);
        CompilationCache cache = new CompilationCache(
                CacheConfig.builder().maximumEntries(1).maximumRetainedWeight(1).build(),
                (source, environment) -> {
                    compilerCalls.incrementAndGet();
                    compilerEntered.countDown();
                    await(releaseCompiler);
                    return new CompilationCache.CachedCompilation(failure(), 2);
                });
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        ExecutorService executor = Executors.newFixedThreadPool(callerCount);
        try {
            CountDownLatch ready = new CountDownLatch(callerCount);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<ExpressionCompilationResult>> futures = IntStream.range(0, callerCount)
                    .mapToObj(ignored -> (Callable<ExpressionCompilationResult>) () -> {
                        ready.countDown();
                        start.await();
                        return cache.get("source", environment);
                    })
                    .map(executor::submit)
                    .toList();

            ready.await();
            start.countDown();
            compilerEntered.await();
            awaitParticipants(cache, "source", environment, callerCount);
            releaseCompiler.countDown();

            ExpressionCompilationResult first = futures.getFirst().get();
            for (Future<ExpressionCompilationResult> future : futures) {
                assertThat(future.get()).isSameAs(first);
            }
            assertThat(compilerCalls.get()).isOne();
            assertThat(cache.get("source", environment))
                    .as("an individually overweight entry is not retained after all joined callers receive it")
                    .isNotSameAs(first);
            assertThat(compilerCalls.get()).isEqualTo(2);
        } finally {
            executor.shutdownNow();
        }
    }

    private static void awaitParticipants(
            CompilationCache cache, String source, ExpressionEnvironment environment, int expectedCount) {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (cache.inFlightParticipantCount(source, environment) < expectedCount && System.nanoTime() < deadline) {
            Thread.onSpinWait();
        }
        assertThat(cache.inFlightParticipantCount(source, environment)).isEqualTo(expectedCount);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }

    private static ExpressionCompilationResult.Failure failure() {
        return new ExpressionCompilationResult.Failure(List.of(ExpressionDiagnostic.error(
                DiagnosticCategory.SEMANTIC, "TEST_CODE", "test failure", null)));
    }
}
