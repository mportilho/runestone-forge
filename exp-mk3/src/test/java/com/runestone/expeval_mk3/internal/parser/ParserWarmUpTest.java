package com.runestone.expeval_mk3.internal.parser;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class ParserWarmUpTest {

    @Test
    void sharedWarmUpUsesTheRealParserAndOnlyExposesTheModuleFacade() {
        assertThat(ParserWarmUp.shared()).isSameAs(ParserWarmUp.shared());

        ParserWarmUp.shared().ensureWarmedUp();
        ParserWarmUp.shared().ensureWarmedUp();
    }

    @Test
    void concurrentCallersRunTheWarmUpWorkExactlyOnce() throws InterruptedException {
        int threadCount = 32;
        AtomicInteger invocations = new AtomicInteger();
        ParserWarmUp warmUp = new ParserWarmUp(invocations::incrementAndGet);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        try {
            IntStream.range(0, threadCount).forEach(ignored -> executor.submit(() -> {
                ready.countDown();
                awaitUninterruptibly(start);
                warmUp.ensureWarmedUp();
            }));
            ready.await();
            start.countDown();
        } finally {
            executor.shutdown();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(invocations.get()).isEqualTo(1);
    }

    @Test
    void aFailingWarmUpSourcePropagatesAsAnInitializationBugRatherThanBeingSwallowed() {
        ParserWarmUp warmUp = new ParserWarmUp(() -> {
            throw new IllegalStateException("representative source stopped parsing");
        });

        assertThatIllegalStateException()
                .isThrownBy(warmUp::ensureWarmedUp)
                .withMessage("representative source stopped parsing");
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
