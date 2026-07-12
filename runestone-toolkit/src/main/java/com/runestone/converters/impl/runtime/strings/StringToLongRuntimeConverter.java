package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

public final class StringToLongRuntimeConverter extends StringRuntimeConverter<Long> {

    public StringToLongRuntimeConverter() {
        super(Long.class);
    }

    @Override
    public Long convert(String source, ConversionContext context) {
        return Long.valueOf(source);
    }
}
