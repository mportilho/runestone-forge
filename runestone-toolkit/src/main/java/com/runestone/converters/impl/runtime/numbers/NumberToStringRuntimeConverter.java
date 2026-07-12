package com.runestone.converters.impl.runtime.numbers;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;

import java.math.BigDecimal;

public final class NumberToStringRuntimeConverter implements RuntimeDataConverter<Number, String> {

    @Override
    public Class<Number> sourceType() {
        return Number.class;
    }

    @Override
    public Class<String> targetType() {
        return String.class;
    }

    @Override
    public String convert(Number source, ConversionContext context) {
        return switch (source) {
            case BigDecimal bigDecimal -> bigDecimal.toPlainString();
            default -> source.toString();
        };
    }
}
