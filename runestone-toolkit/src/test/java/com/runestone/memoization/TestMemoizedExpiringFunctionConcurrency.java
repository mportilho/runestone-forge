package com.runestone.memoization;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TestMemoizedExpiringFunctionConcurrency {

    @Test
    public void testConcurrentColdStartAccess() throws InterruptedException {
        int numThreads = 50;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        AtomicInteger delegateCallCount = new AtomicInteger(0);

        MemoizedExpiringFunction<Integer, String> memoizedFunction = new MemoizedExpiringFunction<>(n -> {
            delegateCallCount.incrementAndGet();
            try {
                // Simulate slow computation
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Value" + n;
        }, 1, TimeUnit.MINUTES);

        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    String result = memoizedFunction.apply(42);
                    Assertions.assertThat(result).isEqualTo("Value42");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads at the same time
        boolean completed = doneLatch.await(5, TimeUnit.SECONDS);

        executorService.shutdownNow();

        Assertions.assertThat(completed).isTrue();
        // Since all threads asked for the same key '42' concurrently,
        // ideally the delegate should be called EXACTLY ONCE.
        Assertions.assertThat(delegateCallCount.get()).isEqualTo(1);
    }
}
