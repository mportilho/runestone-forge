package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

public class StringToByteConverter implements DataConverter<String, Byte> {

    @Override
    public Byte convert(String data) {
        return Byte.valueOf(data);
    }
}
