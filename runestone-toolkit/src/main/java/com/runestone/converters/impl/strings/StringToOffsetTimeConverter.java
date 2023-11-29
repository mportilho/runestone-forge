package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;
import com.runestone.utils.DateUtils;

import java.time.OffsetTime;

public class StringToOffsetTimeConverter implements DataConverter<String, OffsetTime> {

    @Override
    public OffsetTime convert(String data) {
        return DateUtils.DATETIME_FORMATTER.parse(data, OffsetTime::from);
    }
}
