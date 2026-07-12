package com.runestone.converters.impl.runtime.numbers;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;

public final class NumberToLongRuntimeConverter implements RuntimeDataConverter<Number, Long> {

    @Override
    public Class<Number> sourceType() {
        return Number.class;
    }

    @Override
    public Class<Long> targetType() {
        return Long.class;
    }

    @Override
    public Long convert(Number source, ConversionContext context) {
        return source.longValue();
    }
}
