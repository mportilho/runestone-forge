package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;
import com.runestone.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;

public class StringToTemporalConverter implements DataConverter<String, Temporal> {

    @Override
    public Temporal convert(String data) {
        if (data.length() <= 8) {
            return DateUtils.DATETIME_FORMATTER.parse(data, LocalTime::from);
        } else if (data.length() <= 10) {
            return DateUtils.DATETIME_FORMATTER.parse(data, LocalDate::from);
        } else {
            return DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse(data, LocalDateTime::from);
        }
    }
}
