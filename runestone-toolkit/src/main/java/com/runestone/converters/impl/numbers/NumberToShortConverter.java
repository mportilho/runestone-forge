package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;

public class NumberToShortConverter implements DataConverter<Number, Short> {

    @Override
    public Short convert(Number data) {
        return switch (data) {
            case Short s -> s;
            case Number n -> n.shortValue();
            case null -> throw new IllegalArgumentException("Cannot convert null to Short");
        };
    }

}
