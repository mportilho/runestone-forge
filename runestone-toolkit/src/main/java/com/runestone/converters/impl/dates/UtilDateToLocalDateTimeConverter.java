package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.time.LocalDateTime;
import java.util.Date;

public class UtilDateToLocalDateTimeConverter implements DataConverter<Date, LocalDateTime> {

    @Override
    public LocalDateTime convert(Date data) {
        return new java.sql.Timestamp(data.getTime()).toLocalDateTime();
    }
}
