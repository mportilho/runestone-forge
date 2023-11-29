package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;
import com.runestone.utils.DateUtils;

import java.time.LocalDate;

public class StringToLocalDateConverter implements DataConverter<String, LocalDate> {

    @Override
    public LocalDate convert(String data) {
        return DateUtils.DATETIME_FORMATTER.parse(data, LocalDate::from);
    }
}
