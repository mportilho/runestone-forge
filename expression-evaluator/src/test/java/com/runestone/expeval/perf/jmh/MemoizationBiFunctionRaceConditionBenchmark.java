package com.runestone.expeval.perf.jmh;

import com.runestone.memoization.MemoizedBiFunction;
import com.runestone.memoization.MemoizedExpiringBiFunction;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Benchmark to test the race condition and extra object allocation during cold start 
 * in MemoizedBiFunction and MemoizedExpiringBiFunction.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(value = 1, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
@Threads(16) // High contention
public class MemoizationBiFunctionRaceConditionBenchmark {

    private MemoizedExpiringBiFunction<String, String, String> expiringCache;
    private MemoizedBiFunction<String, String, String> regularCache;
    private AtomicInteger expiringEvaluationCount;
    private AtomicInteger regularEvaluationCount;

    @Setup(Level.Iteration)
    public void setup() {
        expiringEvaluationCount = new AtomicInteger();
        regularEvaluationCount = new AtomicInteger();
        
        // Simulate a 1ms expensive parsing operation
        expiringCache = new MemoizedExpiringBiFunction<>((s1, s2) -> {
            expiringEvaluationCount.incrementAndGet();
            Blackhole.consumeCPU(10_000); // simulate work
            return s1 + s2;
        }, 1, TimeUnit.MINUTES);

        regularCache = new MemoizedBiFunction<String, String, String>((String s1, String s2) -> {
            regularEvaluationCount.incrementAndGet();
            Blackhole.consumeCPU(10_000); // simulate work
            return s1 + s2;
        });
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        System.out.println(" Expiring Cache Evaluations in this iteration: " + expiringEvaluationCount.get());
        System.out.println(" Regular Cache Evaluations in this iteration: " + regularEvaluationCount.get());
    }

    @Benchmark
    public void testConcurrentColdAccessExpiring(Blackhole blackhole) {
        // All 16 threads hit the same key simultaneously
        blackhole.consume(expiringCache.apply("key1", "key2"));
    }

    @Benchmark
    public void testConcurrentColdAccessRegular(Blackhole blackhole) {
        // All 16 threads hit the same key simultaneously
        blackhole.consume(regularCache.apply("key1", "key2"));
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
