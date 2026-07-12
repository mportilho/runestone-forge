package com.runestone.converters.impl.runtime.numbers;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;

public final class NumberToFloatRuntimeConverter implements RuntimeDataConverter<Number, Float> {

    @Override
    public Class<Number> sourceType() {
        return Number.class;
    }

    @Override
    public Class<Float> targetType() {
        return Float.class;
    }

    @Override
    public Float convert(Number source, ConversionContext context) {
        return source.floatValue();
    }
}
