package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.time.LocalDate;
import java.util.Date;

public class UtilDateToLocalDateConverter implements DataConverter<Date, LocalDate> {

    @Override
    public LocalDate convert(Date data) {
        return new java.sql.Date(data.getTime()).toLocalDate();
    }
}
