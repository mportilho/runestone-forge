package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

import java.math.BigDecimal;

public class StringToBigDecimalConverter implements DataConverter<String, BigDecimal> {

    @Override
    public BigDecimal convert(String data) {
        return new BigDecimal(data);
    }
}
