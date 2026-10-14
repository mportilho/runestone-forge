package com.runestone.converters.impl.runtime.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

final class RuntimeNumberConversionSupport {

    private RuntimeNumberConversionSupport() {
    }

    static BigDecimal toBigDecimal(Number source) {
        return switch (source) {
            case Byte value -> BigDecimal.valueOf(value);
            case Short value -> BigDecimal.valueOf(value);
            case Integer value -> BigDecimal.valueOf(value);
            case Long value -> BigDecimal.valueOf(value);
            case Float value -> new BigDecimal(String.valueOf(value));
            case Double value -> new BigDecimal(String.valueOf(value));
            case AtomicInteger value -> BigDecimal.valueOf(value.get());
            case AtomicLong value -> BigDecimal.valueOf(value.get());
            case DoubleAccumulator value -> new BigDecimal(String.valueOf(value.get()));
            case DoubleAdder value -> new BigDecimal(String.valueOf(value.sum()));
            case LongAccumulator value -> BigDecimal.valueOf(value.get());
            case LongAdder value -> BigDecimal.valueOf(value.sum());
            case BigInteger value -> new BigDecimal(value);
            case BigDecimal value -> value;
            default -> throw new IllegalArgumentException("Cannot convert " + source.getClass().getName()
                    + " to BigDecimal");
        };
    }
}
