package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

public final class StringToClassRuntimeConverter extends StringRuntimeConverter<Class<?>> {

    public StringToClassRuntimeConverter() {
        super(Class.class);
    }

    @Override
    public Class<?> convert(String source, ConversionContext context) {
        try {
            return Class.forName(source);
        } catch (ClassNotFoundException exception) {
            throw new IllegalArgumentException("Class not found: " + source, exception);
        }
    }
}
