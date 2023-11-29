package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;

public class NumberToIntConverter implements DataConverter<Number, Integer> {

    @Override
    public Integer convert(Number data) {
        return switch (data) {
            case Integer i -> i;
            case Number n -> n.intValue();
            case null -> throw new IllegalArgumentException("Cannot convert null to Integer");
        };
    }

}
