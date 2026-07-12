package com.runestone.converters.impl.runtime.numbers;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;

public final class NumberToByteRuntimeConverter implements RuntimeDataConverter<Number, Byte> {

    @Override
    public Class<Number> sourceType() {
        return Number.class;
    }

    @Override
    public Class<Byte> targetType() {
        return Byte.class;
    }

    @Override
    public Byte convert(Number source, ConversionContext context) {
        return source.byteValue();
    }
}
