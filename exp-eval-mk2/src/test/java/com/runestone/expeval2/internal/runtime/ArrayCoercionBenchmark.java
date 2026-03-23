package com.runestone.expeval2.internal.runtime;

import com.runestone.converters.impl.DefaultDataConversionService;
import org.openjdk.jmh.annotations.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
@State(Scope.Benchmark)
public class ArrayCoercionBenchmark {

    private RuntimeCoercionService coercionService;
    private List<Object> bigDecimalVector;
    private List<Object> doubleVector;

    @Setup
    public void setup() {
        coercionService = new RuntimeCoercionService(new DefaultDataConversionService());

        bigDecimalVector = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            bigDecimalVector.add(BigDecimal.valueOf(i));
        }

        doubleVector = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            doubleVector.add(BigDecimal.valueOf(i));
        }
    }

    @Benchmark
    public Object coerceToBigDecimalArray() {
        return coercionService.coerce(bigDecimalVector, BigDecimal[].class);
    }

    @Benchmark
    public Object coerceToDoublePrimitiveArray() {
        return coercionService.coerce(doubleVector, double[].class);
    }
}
