package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

public class StringToShortConverter implements DataConverter<String, Short> {

    @Override
    public Short convert(String data) {
        return Short.parseShort(data);
    }
}
