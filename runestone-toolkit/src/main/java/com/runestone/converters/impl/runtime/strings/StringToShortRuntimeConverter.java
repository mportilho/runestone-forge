package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

public final class StringToShortRuntimeConverter extends StringRuntimeConverter<Short> {

    public StringToShortRuntimeConverter() {
        super(Short.class);
    }

    @Override
    public Short convert(String source, ConversionContext context) {
        return Short.parseShort(source);
    }
}
