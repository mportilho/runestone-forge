package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;
import com.runestone.utils.DateUtils;

import java.time.LocalTime;

public class StringToLocalTimeConverter implements DataConverter<String, LocalTime> {

    @Override
    public LocalTime convert(String data) {
        return DateUtils.DATETIME_FORMATTER.parse(data, LocalTime::from);
    }
}
