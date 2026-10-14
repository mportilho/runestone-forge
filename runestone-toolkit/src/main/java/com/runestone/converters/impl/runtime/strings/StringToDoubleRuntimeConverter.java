package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

public final class StringToDoubleRuntimeConverter extends StringRuntimeConverter<Double> {

    public StringToDoubleRuntimeConverter() {
        super(Double.class);
    }

    @Override
    public Double convert(String source, ConversionContext context) {
        return Double.valueOf(source);
    }
}
