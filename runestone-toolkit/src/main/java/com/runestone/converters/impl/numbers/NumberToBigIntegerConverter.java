package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;

import java.math.BigInteger;

public class NumberToBigIntegerConverter implements DataConverter<Number, BigInteger> {

    @Override
    public BigInteger convert(Number data) {
        return switch (data) {
            case BigInteger bi -> bi;
            case Number n -> BigInteger.valueOf(n.longValue());
            case null -> throw new IllegalArgumentException("Cannot convert null to BigInteger");
        };
    }
}
