package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;

public class NumberToLongConverter implements DataConverter<Number, Long> {

    @Override
    public Long convert(Number data) {
        return switch (data) {
            case Long l -> l;
            case Number n -> n.longValue();
            case null -> throw new IllegalArgumentException("Cannot convert null to Long");
        };
    }

}
