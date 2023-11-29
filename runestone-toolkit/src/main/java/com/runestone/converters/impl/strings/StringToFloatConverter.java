package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

public class StringToFloatConverter implements DataConverter<String, Float> {

    @Override
    public Float convert(String data) {
        return Float.valueOf(data);
    }
}
