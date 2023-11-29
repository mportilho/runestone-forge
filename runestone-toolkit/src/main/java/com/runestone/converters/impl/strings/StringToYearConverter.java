package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

import java.time.Year;

public class StringToYearConverter implements DataConverter<String, Year> {

    @Override
    public Year convert(String data) {
        return Year.parse(data);
    }
}
