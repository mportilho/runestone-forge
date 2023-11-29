package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;

public class StringToJavaUtilDateConverter implements DataConverter<String, Date> {

    @Override
    public Date convert(String data) {
        Temporal temporal = StringConversionUtils.convertTemporal(data);
        if (temporal instanceof LocalDate localDate) {
            return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        } else if (temporal instanceof LocalDateTime localDateTime) {
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        }
        return Date.from(((ZonedDateTime) temporal).toInstant());
    }

}
