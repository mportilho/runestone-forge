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
    private RuntimeValue.VectorValue bigDecimalVector;
    private RuntimeValue.VectorValue doubleVector;

    @Setup
    public void setup() {
        coercionService = new RuntimeCoercionService(new DefaultDataConversionService());
        
        List<RuntimeValue> bdElements = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            bdElements.add(new RuntimeValue.NumberValue(BigDecimal.valueOf(i)));
        }
        bigDecimalVector = new RuntimeValue.VectorValue(bdElements);

        List<RuntimeValue> dElements = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            dElements.add(new RuntimeValue.NumberValue(BigDecimal.valueOf(i)));
        }
        doubleVector = new RuntimeValue.VectorValue(dElements);
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
