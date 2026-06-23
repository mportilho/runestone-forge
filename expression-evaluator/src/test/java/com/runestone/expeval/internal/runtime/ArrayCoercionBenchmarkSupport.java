package com.runestone.expeval.internal.runtime;

import com.runestone.converters.impl.DefaultDataConversionService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class ArrayCoercionBenchmarkSupport {

    private final RuntimeCoercionService coercionService;
    private final List<Object> bigDecimalVector;
    private final List<Object> doubleVector;

    public ArrayCoercionBenchmarkSupport() {
        coercionService = new RuntimeCoercionService(new DefaultDataConversionService());
        bigDecimalVector = newVector();
        doubleVector = newVector();
    }

    public Object coerceToBigDecimalArray() {
        return coercionService.coerce(bigDecimalVector, BigDecimal[].class);
    }

    public Object coerceToDoublePrimitiveArray() {
        return coercionService.coerce(doubleVector, double[].class);
    }

    private static List<Object> newVector() {
        List<Object> vector = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            vector.add(BigDecimal.valueOf(i));
        }
        return vector;
    }
}
