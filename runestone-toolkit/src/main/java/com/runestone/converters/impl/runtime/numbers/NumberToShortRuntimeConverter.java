package com.runestone.converters.impl.runtime.numbers;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;

public final class NumberToShortRuntimeConverter implements RuntimeDataConverter<Number, Short> {

    @Override
    public Class<Number> sourceType() {
        return Number.class;
    }

    @Override
    public Class<Short> targetType() {
        return Short.class;
    }

    @Override
    public Short convert(Number source, ConversionContext context) {
        return source.shortValue();
    }
}
