package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.util.Currency;

public final class StringToCurrencyRuntimeConverter extends StringRuntimeConverter<Currency> {

    public StringToCurrencyRuntimeConverter() {
        super(Currency.class);
    }

    @Override
    public Currency convert(String source, ConversionContext context) {
        return Currency.getInstance(source);
    }
}
