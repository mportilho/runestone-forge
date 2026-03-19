package com.runestone.expeval2.perf.jmh;

import com.runestone.expeval2.internal.runtime.VectorMaterializationBenchmarkSupport;
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
public class VectorMaterializationBenchmark {

    @Param({"baseline", "optimized"})
    private String implementation;

    @Param({"128"})
    private int vectorSize;

    private VectorMaterializationBenchmarkSupport support;

    @Setup
    public void setup() {
        support = new VectorMaterializationBenchmarkSupport(vectorSize);
    }

    @Benchmark
    public Object wrapMutableList() {
        return implementation.equals("baseline")
                ? support.baselineWrapMutableList()
                : support.optimizedWrapMutableList();
    }

    @Benchmark
    public Object evaluateVectorLiteral() {
        return implementation.equals("baseline")
                ? support.baselineEvaluateVectorLiteral()
                : support.optimizedEvaluateVectorLiteral();
    }
}
