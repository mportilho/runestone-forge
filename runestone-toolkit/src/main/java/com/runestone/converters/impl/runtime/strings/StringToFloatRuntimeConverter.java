package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

public final class StringToFloatRuntimeConverter extends StringRuntimeConverter<Float> {

    public StringToFloatRuntimeConverter() {
        super(Float.class);
    }

    @Override
    public Float convert(String source, ConversionContext context) {
        return Float.valueOf(source);
    }
}
