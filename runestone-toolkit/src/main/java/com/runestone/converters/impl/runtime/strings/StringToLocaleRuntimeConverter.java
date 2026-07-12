package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.util.Locale;

public final class StringToLocaleRuntimeConverter extends StringRuntimeConverter<Locale> {

    public StringToLocaleRuntimeConverter() {
        super(Locale.class);
    }

    @Override
    public Locale convert(String source, ConversionContext context) {
        String languageTag = source.trim().replace('_', '-');
        if (languageTag.isEmpty()) {
            throw new IllegalArgumentException("Locale language tag must not be blank");
        }
        Locale locale = Locale.forLanguageTag(languageTag);
        if (locale.toLanguageTag().equals("und")) {
            throw new IllegalArgumentException("Invalid locale language tag: " + source);
        }
        return locale;
    }
}
