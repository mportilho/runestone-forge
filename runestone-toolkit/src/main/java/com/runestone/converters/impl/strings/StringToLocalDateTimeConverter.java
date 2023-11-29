package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;
import com.runestone.utils.DateUtils;

import java.time.LocalDateTime;

public class StringToLocalDateTimeConverter implements DataConverter<String, LocalDateTime> {

    @Override
    public LocalDateTime convert(String data) {
        return DateUtils.DATETIME_FORMATTER.parse(data, LocalDateTime::from);
    }
}
