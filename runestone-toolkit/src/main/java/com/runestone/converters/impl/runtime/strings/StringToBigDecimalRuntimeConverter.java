package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.math.BigDecimal;

public final class StringToBigDecimalRuntimeConverter extends StringRuntimeConverter<BigDecimal> {

    public StringToBigDecimalRuntimeConverter() {
        super(BigDecimal.class);
    }

    @Override
    public BigDecimal convert(String source, ConversionContext context) {
        return new BigDecimal(source);
    }
}
