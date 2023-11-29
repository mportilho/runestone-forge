package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

public class StringToJavaSqlDateConverter implements DataConverter<String, Date> {

    @Override
    public Date convert(String data) {
        Temporal temporal = StringConversionUtils.convertTemporal(data);
        if (temporal instanceof LocalDate localDate) {
            return Date.valueOf(localDate);
        } else if (temporal instanceof LocalDateTime localDateTime) {
            return Date.valueOf(localDateTime.toLocalDate());
        }
        return Date.valueOf(((ZonedDateTime) temporal).toLocalDate());
    }
}
