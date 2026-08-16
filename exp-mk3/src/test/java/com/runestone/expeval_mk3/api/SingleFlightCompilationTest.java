package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.cache.CompilationCache;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #135: concurrent callers for the same {@code (source, environmentId)} run the compiler exactly
 * once for the currently resident generation, in success and in both failure kinds, and every caller
 * receives the same resident {@link ExpressionCompilationResult}. Each single-flight proof counts genuine
 * invocations of {@link CompilationPipeline#compile} with an {@link AtomicInteger} and holds the winning
 * caller inside the compiler with a {@link CountDownLatch} until every other caller has been dispatched;
 * none of this depends on wall-clock timing or on Caffeine's internal implementation, only on the
 * documented atomicity of {@code Cache#get(key, mappingFunction)}. A final test goes through
 * {@link ExpressionEngine} itself, without instrumentation, to prove the same result identity holds
 * through the public compilation boundary and not only at the cache seam.
 */
class SingleFlightCompilationTest {

    @Test
    void concurrentCallersOfTheSameKeyRunTheCompilerOnceOnSuccess() throws Exception {
        assertSingleFlight("1 + 2", ExpressionCompilationResult.Success.class);
    }

    @Test
    void concurrentCallersOfTheSameKeyRunTheCompilerOnceOnSyntaxFailure() throws Exception {
        assertSingleFlight("1 +", ExpressionCompilationResult.Failure.class);
    }

    @Test
    void concurrentCallersOfTheSameKeyRunTheCompilerOnceOnSemanticFailure() throws Exception {
        assertSingleFlight("missing", ExpressionCompilationResult.Failure.class);
    }

    @Test
    void concurrentCallersThroughTheEngineItselfAllReceiveTheSameResidentResult() throws Exception {
        ExpressionEngine engine = ExpressionEngine.builder().build();
        ExpressionEnvironment environment = ExpressionEnvironment.standard();
        int callerCount = 32;
        ExecutorService executor = Executors.newFixedThreadPool(callerCount);
        try {
            CountDownLatch ready = new CountDownLatch(callerCount);
            CountDownLatch start = new CountDownLatch(1);
            List<Callable<ExpressionCompilationResult>> calls = IntStream.range(0, callerCount)
                    .mapToObj(value -> (Callable<ExpressionCompilationResult>) () -> {
                        ready.countDown();
                        start.await();
                        return engine.compile("1 + 2", environment);
                    })
                    .collect(Collectors.toList());
            List<Future<ExpressionCompilationResult>> futures =
                    calls.stream().map(executor::submit).collect(Collectors.toList());

            ready.await();
            start.countDown();

            ExpressionCompilationResult first = futures.get(0).get();
            for (Future<ExpressionCompilationResult> future : futures) {
                assertThat(future.get()).isSameAs(first);
            }
            assertThat(first).isInstanceOf(ExpressionCompilationResult.Success.class);
        } finally {
            executor.shutdownNow();
        }
    }

    private void assertSingleFlight(String source, Class<? extends ExpressionCompilationResult> expectedType)
            throws Exception {
        int callerCount = 32;
        AtomicInteger compilerCalls = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        RuntimeServices runtimeServices = RuntimeServices.systemDefault();
        CompilationCache cache = new CompilationCache(CacheConfig.defaults(), (compiledSource, environment) -> {
            compilerCalls.incrementAndGet();
            entered.countDown();
            awaitUninterruptibly(release);
            return CompilationPipeline.compile(compiledSource, environment, runtimeServices);
        });
        ExpressionEnvironment environment = ExpressionEnvironment.standard();
        ExecutorService executor = Executors.newFixedThreadPool(callerCount);
        try {
            CountDownLatch ready = new CountDownLatch(callerCount);
            CountDownLatch start = new CountDownLatch(1);
            List<Callable<ExpressionCompilationResult>> calls = IntStream.range(0, callerCount)
                    .mapToObj(value -> (Callable<ExpressionCompilationResult>) () -> {
                        ready.countDown();
                        start.await();
                        return cache.get(source, environment);
                    })
                    .collect(Collectors.toList());
            List<Future<ExpressionCompilationResult>> futures =
                    calls.stream().map(executor::submit).collect(Collectors.toList());

            ready.await();
            start.countDown();
            entered.await();
            release.countDown();

            ExpressionCompilationResult first = futures.get(0).get();
            for (Future<ExpressionCompilationResult> future : futures) {
                assertThat(future.get()).isSameAs(first);
            }
            assertThat(first).isInstanceOf(expectedType);
            assertThat(compilerCalls.get()).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    private static void awaitUninterruptibly(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
