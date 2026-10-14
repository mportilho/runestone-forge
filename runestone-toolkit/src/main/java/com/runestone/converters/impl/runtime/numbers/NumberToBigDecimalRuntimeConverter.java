package com.runestone.converters.impl.runtime.numbers;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;

import java.math.BigDecimal;

public final class NumberToBigDecimalRuntimeConverter implements RuntimeDataConverter<Number, BigDecimal> {

    @Override
    public Class<Number> sourceType() {
        return Number.class;
    }

    @Override
    public Class<BigDecimal> targetType() {
        return BigDecimal.class;
    }

    @Override
    public BigDecimal convert(Number source, ConversionContext context) {
        return RuntimeNumberConversionSupport.toBigDecimal(source);
    }
}
