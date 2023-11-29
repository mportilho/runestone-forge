package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class UtilDateToZonedDateTimeConverter implements DataConverter<Date, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(Date data) {
        return ZonedDateTime.of(new java.sql.Timestamp(data.getTime()).toLocalDateTime(), ZoneId.systemDefault());
    }
}
