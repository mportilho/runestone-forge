package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

public class StringToDoubleConverter implements DataConverter<String, Double> {

    @Override
    public Double convert(String data) {
        return Double.valueOf(data);
    }
}
