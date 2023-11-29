package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

import java.util.Objects;

public class StringToBooleanConverter implements DataConverter<String, Boolean> {

    @Override
    public Boolean convert(String data) {
        Objects.requireNonNull(data);
        return switch (data.toLowerCase()) {
            case "true", "on", "yes", "1" -> Boolean.TRUE;
            default -> Boolean.FALSE;
        };
    }
}
