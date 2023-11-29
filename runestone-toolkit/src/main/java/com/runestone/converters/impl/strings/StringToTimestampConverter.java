package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

public class StringToTimestampConverter implements DataConverter<String, Timestamp> {

    @Override
    public Timestamp convert(String data) {
        Temporal temporal = StringConversionUtils.convertTemporal(data);
        if (temporal instanceof LocalDate localDate) {
            return Timestamp.valueOf(localDate.atTime(LocalTime.MIDNIGHT));
        } else if (temporal instanceof LocalDateTime localDateTime) {
            return Timestamp.valueOf(localDateTime);
        }
        return Timestamp.from(((ZonedDateTime) temporal).toInstant());
    }
}
