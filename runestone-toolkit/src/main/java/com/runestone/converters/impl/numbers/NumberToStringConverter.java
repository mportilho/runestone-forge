package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;

import java.math.BigDecimal;

public class NumberToStringConverter implements DataConverter<Number, String> {

    @Override
    public String convert(Number data) {
        return switch (data) {
            case BigDecimal n -> n.toPlainString();
            case Number n -> n.toString();
        };
    }
}
