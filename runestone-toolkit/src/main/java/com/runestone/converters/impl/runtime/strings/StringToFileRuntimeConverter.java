package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.io.File;

public final class StringToFileRuntimeConverter extends StringRuntimeConverter<File> {

    public StringToFileRuntimeConverter() {
        super(File.class);
    }

    @Override
    public File convert(String source, ConversionContext context) {
        return new File(source);
    }
}
