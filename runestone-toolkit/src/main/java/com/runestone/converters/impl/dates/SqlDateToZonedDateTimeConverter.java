package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SqlDateToZonedDateTimeConverter implements DataConverter<Date, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(Date data) {
        return ZonedDateTime.of(new Timestamp(data.getTime()).toLocalDateTime(), ZoneId.systemDefault());
    }
}
