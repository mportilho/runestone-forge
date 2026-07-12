package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.nio.file.Path;

public final class StringToPathRuntimeConverter extends StringRuntimeConverter<Path> {

    public StringToPathRuntimeConverter() {
        super(Path.class);
    }

    @Override
    public Path convert(String source, ConversionContext context) {
        return Path.of(source);
    }
}
