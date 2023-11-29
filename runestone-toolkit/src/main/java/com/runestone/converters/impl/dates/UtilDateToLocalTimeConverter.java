package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.time.LocalTime;
import java.util.Date;

public class UtilDateToLocalTimeConverter implements DataConverter<Date, LocalTime> {

    @Override
    public LocalTime convert(Date data) {
        return new java.sql.Timestamp(data.getTime()).toLocalDateTime().toLocalTime();
    }
}
