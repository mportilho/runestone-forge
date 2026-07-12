package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.util.Base64;

public final class StringToByteArrayRuntimeConverter extends StringRuntimeConverter<byte[]> {

    public StringToByteArrayRuntimeConverter() {
        super(byte[].class);
    }

    @Override
    public byte[] convert(String source, ConversionContext context) {
        return Base64.getDecoder().decode(source);
    }
}
