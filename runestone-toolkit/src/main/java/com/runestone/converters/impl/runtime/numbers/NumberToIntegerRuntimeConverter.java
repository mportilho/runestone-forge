package com.runestone.converters.impl.runtime.numbers;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;

public final class NumberToIntegerRuntimeConverter implements RuntimeDataConverter<Number, Integer> {

    @Override
    public Class<Number> sourceType() {
        return Number.class;
    }

    @Override
    public Class<Integer> targetType() {
        return Integer.class;
    }

    @Override
    public Integer convert(Number source, ConversionContext context) {
        return source.intValue();
    }
}
