package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.util.regex.Pattern;

public final class StringToPatternRuntimeConverter extends StringRuntimeConverter<Pattern> {

    public StringToPatternRuntimeConverter() {
        super(Pattern.class);
    }

    @Override
    public Pattern convert(String source, ConversionContext context) {
        return Pattern.compile(source);
    }
}
