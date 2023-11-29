package com.runestone.converters.impl.strings;

import com.runestone.converters.DataConverter;

import java.math.BigInteger;

public class StringToBigIntegerConverter implements DataConverter<String, BigInteger> {

    @Override
    public BigInteger convert(String data) {
        return new BigInteger(data);
    }
}
