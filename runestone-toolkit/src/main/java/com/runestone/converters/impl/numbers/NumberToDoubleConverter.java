package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;

public class NumberToDoubleConverter implements DataConverter<Number, Double> {

    @Override
    public Double convert(Number data) {
        return switch (data) {
            case Double d -> d;
            case Number n -> n.doubleValue();
            case null -> throw new IllegalArgumentException("Cannot convert null to Double");
        };
    }
}
