package com.runestone.memoization;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@Threads(4)
public class MemoizationBenchmark {

    private Function<Integer, String> concurrentHashMapMemo;
    private Function<Integer, String> caffeineMemo;

    @Setup
    public void setup() {
        Map<Integer, String> chm = new ConcurrentHashMap<>();
        concurrentHashMapMemo = (key) -> chm.computeIfAbsent(key, k -> "value" + k);
        
        caffeineMemo = new MemoizedFunction<>((key) -> "value" + key);
    }

    @Benchmark
    public String testConcurrentHashMap(ThreadState state) {
        return concurrentHashMapMemo.apply(state.index++ % 1000);
    }

    @Benchmark
    public String testCaffeineMemo(ThreadState state) {
        return caffeineMemo.apply(state.index++ % 1000);
    }

    @State(Scope.Thread)
    public static class ThreadState {
        int index = 0;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MemoizationBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
