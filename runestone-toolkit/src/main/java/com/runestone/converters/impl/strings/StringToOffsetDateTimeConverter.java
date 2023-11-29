package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;
import com.runestone.utils.DateUtils;

import java.time.OffsetDateTime;

public class StringToOffsetDateTimeConverter implements DataConverter<String, OffsetDateTime> {

    @Override
    public OffsetDateTime convert(String data) {
        return DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse(data, OffsetDateTime::from);
    }
}
