package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

public final class StringToByteRuntimeConverter extends StringRuntimeConverter<Byte> {

    public StringToByteRuntimeConverter() {
        super(Byte.class);
    }

    @Override
    public Byte convert(String source, ConversionContext context) {
        return Byte.valueOf(source);
    }
}
