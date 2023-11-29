package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

public class StringToIntegerConverter implements DataConverter<String, Integer> {

    @Override
    public Integer convert(String data) {
        return Integer.valueOf(data);
    }
}
