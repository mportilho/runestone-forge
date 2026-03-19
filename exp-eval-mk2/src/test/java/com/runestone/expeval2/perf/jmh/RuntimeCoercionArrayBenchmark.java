package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.internal.runtime.RuntimeCoercionArrayBenchmarkSupport;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
public class RuntimeCoercionArrayBenchmark {

    @Param({"baseline", "optimized"})
    private String implementation;

    @Param({"128"})
    private int vectorSize;

    private RuntimeCoercionArrayBenchmarkSupport support;

    @Setup
    public void setup() {
        support = new RuntimeCoercionArrayBenchmarkSupport(vectorSize);
    }

    @Benchmark
    public Object coerceBigDecimalArray() {
        return implementation.equals("baseline")
                ? support.baselineBigDecimalArray()
                : support.optimizedBigDecimalArray();
    }

    @Benchmark
    public Object coerceDoubleArray() {
        return implementation.equals("baseline")
                ? support.baselineDoubleArray()
                : support.optimizedDoubleArray();
    }

    @Benchmark
    public Object coerceComparableArray() {
        return implementation.equals("baseline")
                ? support.baselineComparableArray()
                : support.optimizedComparableArray();
    }
}
