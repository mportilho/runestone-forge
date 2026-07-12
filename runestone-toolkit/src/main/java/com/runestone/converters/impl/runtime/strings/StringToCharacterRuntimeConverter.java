package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

public final class StringToCharacterRuntimeConverter extends StringRuntimeConverter<Character> {

    public StringToCharacterRuntimeConverter() {
        super(Character.class);
    }

    @Override
    public Character convert(String source, ConversionContext context) {
        if (source.isEmpty()) {
            throw new IllegalArgumentException("String must not be empty or null to convert to Character");
        }
        return source.charAt(0);
    }
}
