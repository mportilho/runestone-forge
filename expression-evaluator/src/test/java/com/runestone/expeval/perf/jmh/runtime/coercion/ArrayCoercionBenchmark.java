package com.runestone.expeval.perf.jmh.runtime.coercion;

import com.runestone.expeval.internal.runtime.ArrayCoercionBenchmarkSupport;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
@State(Scope.Benchmark)
public class ArrayCoercionBenchmark {

    private ArrayCoercionBenchmarkSupport support;

    @Setup
    public void setup() {
        support = new ArrayCoercionBenchmarkSupport();
    }

    @Benchmark
    public Object coerceToBigDecimalArray() {
        return support.coerceToBigDecimalArray();
    }

    @Benchmark
    public Object coerceToDoublePrimitiveArray() {
        return support.coerceToDoublePrimitiveArray();
    }
}
