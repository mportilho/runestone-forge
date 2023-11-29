package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;

public class NumberToFloatConverter implements DataConverter<Number, Float> {

    @Override
    public Float convert(Number data) {
        return switch (data) {
            case Float f -> f;
            case Number n -> n.floatValue();
            case null -> throw new IllegalArgumentException("Cannot convert null to Float");
        };
    }

}