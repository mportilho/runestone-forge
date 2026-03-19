package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.internal.runtime.RuntimeCoercionScalarBenchmarkSupport;
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
public class RuntimeCoercionScalarBenchmark {

    @Param({"baseline", "optimized"})
    private String implementation;

    private RuntimeCoercionScalarBenchmarkSupport support;

    @Setup
    public void setup() {
        support = new RuntimeCoercionScalarBenchmarkSupport();
    }

    @Benchmark
    public double coerceDoublePrimitive() {
        return implementation.equals("baseline")
                ? support.baselineDoublePrimitive()
                : support.optimizedDoublePrimitive();
    }

    @Benchmark
    public Double coerceDoubleWrapper() {
        return implementation.equals("baseline")
                ? support.baselineDoubleWrapper()
                : support.optimizedDoubleWrapper();
    }

    @Benchmark
    public int coerceIntPrimitive() {
        return implementation.equals("baseline")
                ? support.baselineIntPrimitive()
                : support.optimizedIntPrimitive();
    }

    @Benchmark
    public Integer coerceIntWrapper() {
        return implementation.equals("baseline")
                ? support.baselineIntWrapper()
                : support.optimizedIntWrapper();
    }

    @Benchmark
    public long coerceLongPrimitive() {
        return implementation.equals("baseline")
                ? support.baselineLongPrimitive()
                : support.optimizedLongPrimitive();
    }

    @Benchmark
    public Long coerceLongWrapper() {
        return implementation.equals("baseline")
                ? support.baselineLongWrapper()
                : support.optimizedLongWrapper();
    }
}
