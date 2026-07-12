package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.util.UUID;

public final class StringToUUIDRuntimeConverter extends StringRuntimeConverter<UUID> {

    public StringToUUIDRuntimeConverter() {
        super(UUID.class);
    }

    @Override
    public UUID convert(String source, ConversionContext context) {
        return UUID.fromString(source);
    }
}
