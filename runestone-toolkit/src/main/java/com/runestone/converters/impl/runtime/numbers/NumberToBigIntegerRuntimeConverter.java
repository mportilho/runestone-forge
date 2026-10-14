package com.runestone.converters.impl.runtime.numbers;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;

import java.math.BigInteger;

public final class NumberToBigIntegerRuntimeConverter implements RuntimeDataConverter<Number, BigInteger> {

    @Override
    public Class<Number> sourceType() {
        return Number.class;
    }

    @Override
    public Class<BigInteger> targetType() {
        return BigInteger.class;
    }

    @Override
    public BigInteger convert(Number source, ConversionContext context) {
        return BigInteger.valueOf(source.longValue());
    }
}
