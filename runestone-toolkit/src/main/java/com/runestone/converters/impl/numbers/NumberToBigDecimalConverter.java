package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

public class NumberToBigDecimalConverter implements DataConverter<Number, BigDecimal> {

    @Override
    public BigDecimal convert(Number data) {
        return switch (data) {
            case Byte b -> BigDecimal.valueOf(b);
            case Short s -> BigDecimal.valueOf(s);
            case Integer i -> BigDecimal.valueOf(i);
            case Long l -> BigDecimal.valueOf(l);
            case Float f -> BigDecimal.valueOf(f);
            case Double d -> BigDecimal.valueOf(d);
            case AtomicInteger ai -> BigDecimal.valueOf(ai.get());
            case AtomicLong al -> BigDecimal.valueOf(al.get());
            case DoubleAccumulator da -> BigDecimal.valueOf(da.get());
            case DoubleAdder da -> BigDecimal.valueOf(da.sum());
            case LongAccumulator la -> BigDecimal.valueOf(la.get());
            case LongAdder la -> BigDecimal.valueOf(la.sum());
            case BigInteger bi -> new BigDecimal(bi);
            case BigDecimal bd -> bd;
            default ->
                    throw new IllegalArgumentException("Cannot convert " + data.getClass().getName() + " to BigDecimal");
        };
    }
}
