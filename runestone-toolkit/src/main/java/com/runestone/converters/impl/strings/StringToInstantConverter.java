package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;
import com.runestone.utils.DateUtils;

import java.time.Instant;

public class StringToInstantConverter implements DataConverter<String, Instant> {

    @Override
    public Instant convert(String data) {
        return DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse(data, Instant::from);
    }
}
