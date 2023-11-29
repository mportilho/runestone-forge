package com.runestone.converters.impl.strings;

import com.runestone.utils.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

class StringConversionUtils {

    static Temporal convertTemporal(String data) {
        Temporal temporal;
        if (data.length() <= 10) {
            temporal = DateUtils.DATETIME_FORMATTER.parse(data, LocalDate::from);
        } else if (data.length() <= 30) {
            temporal = DateUtils.DATETIME_FORMATTER.parse(data, LocalDateTime::from);
        } else {
            temporal = DateUtils.DATETIME_FORMATTER.parse(data, ZonedDateTime::from);
        }
        return temporal;
    }
}
