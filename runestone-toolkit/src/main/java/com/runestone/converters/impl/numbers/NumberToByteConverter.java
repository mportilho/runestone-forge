package com.runestone.converters.impl.numbers;

import com.runestone.converters.DataConverter;

public class NumberToByteConverter implements DataConverter<Number, Byte> {

    @Override
    public Byte convert(Number data) {
        return switch (data) {
            case Byte b -> b;
            case Number n -> n.byteValue();
            case null -> throw new IllegalArgumentException("Cannot convert null to Byte");
        };
    }
}
