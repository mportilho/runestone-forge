package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.util.Locale;

public final class StringToBooleanRuntimeConverter extends StringRuntimeConverter<Boolean> {

    public StringToBooleanRuntimeConverter() {
        super(Boolean.class);
    }

    @Override
    public Boolean convert(String source, ConversionContext context) {
        return switch (source.trim().toLowerCase(Locale.ROOT)) {
            case "true", "on", "yes", "1" -> Boolean.TRUE;
            default -> Boolean.FALSE;
        };
    }
}
