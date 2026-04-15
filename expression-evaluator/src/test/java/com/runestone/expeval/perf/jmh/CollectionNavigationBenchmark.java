package com.runestone.expeval.perf.jmh;

import com.runestone.expeval.perf.CollectionNavigationBenchmarkSupport;
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

/**
 * Measures the hot collection-navigation paths added by the new implementation.
 *
 * <p>All expressions are pre-compiled in {@link CollectionNavigationBenchmarkSupport}; the
 * benchmark loop only exercises the evaluation phase. Run with the companion runner to collect
 * {@code ns/op} and {@code B/op}.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class CollectionNavigationBenchmark {

    @Benchmark
    public BigDecimal indexAccess(BenchmarkState state) {
        return CollectionNavigationBenchmarkSupport.evaluateIndex(state.index++);
    }

    @Benchmark
    public BigDecimal listFilterCount(BenchmarkState state) {
        return CollectionNavigationBenchmarkSupport.evaluateListFilterCount(state.index++);
    }

    @Benchmark
    public BigDecimal mapFilterCount(BenchmarkState state) {
        return CollectionNavigationBenchmarkSupport.evaluateMapFilterCount(state.index++);
    }

    @Benchmark
    public BigDecimal mapValuesCount(BenchmarkState state) {
        return CollectionNavigationBenchmarkSupport.evaluateMapValuesCount(state.index++);
    }

    @Benchmark
    public BigDecimal customFunctionCount(BenchmarkState state) {
        return CollectionNavigationBenchmarkSupport.evaluateCustomFunctionCount(state.index++);
    }

    @Benchmark
    public BigDecimal deepScanCount(BenchmarkState state) {
        return CollectionNavigationBenchmarkSupport.evaluateDeepScanCount(state.index++);
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
