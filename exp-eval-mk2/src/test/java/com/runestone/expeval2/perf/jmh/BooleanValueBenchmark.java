package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.internal.runtime.BooleanValueBenchmarkSupport;
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

import java.util.concurrent.TimeUnit;

/**
 * Measures allocation from evaluating logical expressions with multiple boolean
 * intermediate results, isolating the {@link com.runestone.expeval2.internal.runtime.RuntimeValue.BooleanValue}
 * allocation pattern.
 *
 * <p>Use {@code -prof gc} to capture {@code B/op}. The {@code boolChain} benchmark (5 comparisons,
 * 4 AND operations) should show ~144 B/op attributable to boolean wrapping:
 * up to 9 new {@code BooleanValue} (16 B each) = 144 B per evaluation.
 *
 * <p>The {@code boolWide} benchmark (10 comparisons, 9 AND operations) exercises a wider
 * boolean chain that prevents short-circuit exits, maximising BooleanValue allocations.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms1g", "-Xmx1g"})
public class BooleanValueBenchmark {

    @Benchmark
    public boolean boolChain(BoolState state) {
        return state.support.evaluateBoolChain();
    }

    @Benchmark
    public boolean boolWide(BoolState state) {
        return state.support.evaluateBoolWide();
    }

    @State(Scope.Thread)
    public static class BoolState {

        BooleanValueBenchmarkSupport support;

        @Setup(Level.Trial)
        public void setUp() {
            support = new BooleanValueBenchmarkSupport();
        }
    }
}
