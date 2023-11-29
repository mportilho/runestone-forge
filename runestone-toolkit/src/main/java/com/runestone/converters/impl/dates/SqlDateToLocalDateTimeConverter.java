package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class SqlDateToLocalDateTimeConverter implements DataConverter<Date, LocalDateTime> {

    @Override
    public LocalDateTime convert(Date data) {
        return new Timestamp(data.getTime()).toLocalDateTime();
    }
}
