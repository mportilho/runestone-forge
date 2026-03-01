package com.runestone.expeval.perf.jmh;

import com.runestone.memoization.MemoizedExpiringFunction;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Benchmark to test the race condition during cold start in MemoizedExpiringFunction.
 *
 * <p>Before the fix, concurrent accesses to the same key during a cache miss would
 * result in redundant evaluations of the delegate function. We simulate an expensive
 * operation (e.g. parsing) and measure the throughput under high thread contention.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(value = 1, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
@Threads(16) // High contention
public class MemoizationRaceConditionBenchmark {

    private MemoizedExpiringFunction<String, Integer> cache;
    private AtomicInteger evaluationCount;

    @Setup(Level.Iteration)
    public void setup() {
        evaluationCount = new AtomicInteger();
        // Simulate a 1ms expensive parsing operation
        cache = new MemoizedExpiringFunction<>(s -> {
            evaluationCount.incrementAndGet();
            Blackhole.consumeCPU(10_000); // simulate work
            return Integer.parseInt(s);
        }, 1, TimeUnit.MINUTES);
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        System.out.println(" Evaluations in this iteration: " + evaluationCount.get());
    }

    @Benchmark
    public void testConcurrentColdAccess(Blackhole blackhole) {
        // All 16 threads hit the same key simultaneously
        blackhole.consume(cache.apply("42"));
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
