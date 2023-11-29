package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

public class StringToLongConverter implements DataConverter<String, Long> {

    @Override
    public Long convert(String data) {
        return Long.valueOf(data);
    }
}
