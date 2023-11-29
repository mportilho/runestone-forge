package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalTime;

public class SqlDateToLocalTimeConverter implements DataConverter<Date, LocalTime> {

    @Override
    public LocalTime convert(Date data) {
        return new Timestamp(data.getTime()).toLocalDateTime().toLocalTime();
    }
}
