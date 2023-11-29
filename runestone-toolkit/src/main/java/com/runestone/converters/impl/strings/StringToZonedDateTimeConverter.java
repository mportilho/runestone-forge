package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;
import com.runestone.utils.DateUtils;

import java.time.ZonedDateTime;

public class StringToZonedDateTimeConverter implements DataConverter<String, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(String data) {
        return DateUtils.DATETIME_FORMATTER.parse(data, ZonedDateTime::from);
    }
}
