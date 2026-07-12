package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

public final class StringToCharsetRuntimeConverter extends StringRuntimeConverter<Charset> {

    public StringToCharsetRuntimeConverter() {
        super(Charset.class);
    }

    @Override
    public Charset convert(String source, ConversionContext context) {
        try {
            return Charset.forName(source);
        } catch (IllegalCharsetNameException | UnsupportedCharsetException exception) {
            throw new IllegalArgumentException("Invalid charset: " + source, exception);
        }
    }
}
