package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.sql.Date;
import java.time.LocalDate;

public class SqlDateToLocalDateConverter implements DataConverter<Date, LocalDate> {

    @Override
    public LocalDate convert(Date data) {
        return data.toLocalDate();
    }
}
