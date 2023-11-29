package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;
import com.runestone.utils.DateUtils;

import java.time.YearMonth;

public class StringToYearMonthConverter implements DataConverter<String, YearMonth> {

    @Override
    public YearMonth convert(String data) {
        return DateUtils.YEAR_MONTH_FORMATTER.parse(data, YearMonth::from);
    }
}
