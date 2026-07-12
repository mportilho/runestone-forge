package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

public final class StringToIntegerRuntimeConverter extends StringRuntimeConverter<Integer> {

    public StringToIntegerRuntimeConverter() {
        super(Integer.class);
    }

    @Override
    public Integer convert(String source, ConversionContext context) {
        return Integer.valueOf(source);
    }
}
