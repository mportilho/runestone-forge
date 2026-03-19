package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.internal.runtime.RuntimeValueFactoryBenchmarkSupport;
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
public class RuntimeValueFactoryBenchmark {

    @Param({"baseline", "optimized"})
    private String implementation;

    @Param({"128"})
    private int vectorSize;

    private RuntimeValueFactoryBenchmarkSupport support;

    @Setup
    public void setup() {
        support = new RuntimeValueFactoryBenchmarkSupport(vectorSize);
    }

    @Benchmark
    public Object fromNumberScalarExact() {
        return implementation.equals("baseline")
                ? support.baselineNumberScalarExact()
                : support.optimizedNumberScalarExact();
    }

    @Benchmark
    public Object fromStringScalarExact() {
        return implementation.equals("baseline")
                ? support.baselineStringScalarExact()
                : support.optimizedStringScalarExact();
    }

    @Benchmark
    public Object fromVectorArray() {
        return implementation.equals("baseline")
                ? support.baselineVectorArray()
                : support.optimizedVectorArray();
    }

    @Benchmark
    public Object fromVectorIterable() {
        return implementation.equals("baseline")
                ? support.baselineVectorIterable()
                : support.optimizedVectorIterable();
    }
}
