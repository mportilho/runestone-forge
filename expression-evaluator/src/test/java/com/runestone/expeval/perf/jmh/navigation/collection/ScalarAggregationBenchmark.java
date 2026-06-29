package com.runestone.expeval.perf.jmh.navigation.collection;

import com.runestone.expeval.perf.ScalarAggregationBenchmarkSupport;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class ScalarAggregationBenchmark {

    @Benchmark
    public BigDecimal directSum(BenchmarkState state) {
        return ScalarAggregationBenchmarkSupport.evaluateDirectSum(state.index++);
    }

    @Benchmark
    public BigDecimal directCount(BenchmarkState state) {
        return ScalarAggregationBenchmarkSupport.evaluateDirectCount(state.index++);
    }

    @Benchmark
    public BigDecimal filterPropertyAvg(BenchmarkState state) {
        return ScalarAggregationBenchmarkSupport.evaluateFilterPropertyAvg(state.index++);
    }

    @Benchmark
    public BigDecimal slicePropertySum(BenchmarkState state) {
        return ScalarAggregationBenchmarkSupport.evaluateSlicePropertySum(state.index++);
    }

    @Benchmark
    public BigDecimal mapTransformSum(BenchmarkState state) {
        return ScalarAggregationBenchmarkSupport.evaluateMapTransformSum(state.index++);
    }

    @Benchmark
    public BigDecimal mapFilterValuesSum(BenchmarkState state) {
        return ScalarAggregationBenchmarkSupport.evaluateMapFilterValuesSum(state.index++);
    }

    @Benchmark
    public BigDecimal countAfterMapTransform(BenchmarkState state) {
        return ScalarAggregationBenchmarkSupport.evaluateCountAfterMapTransform(state.index++);
    }

    @State(Scope.Thread)
    public static class BenchmarkState {

        private int index;

        @Setup(Level.Trial)
        public void setUp() {
            index = 0;
        }
    }
}
