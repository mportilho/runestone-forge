package com.runestone.converters.impl.runtime.numbers;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;

public final class NumberToDoubleRuntimeConverter implements RuntimeDataConverter<Number, Double> {

    @Override
    public Class<Number> sourceType() {
        return Number.class;
    }

    @Override
    public Class<Double> targetType() {
        return Double.class;
    }

    @Override
    public Double convert(Number source, ConversionContext context) {
        return source.doubleValue();
    }
}
