package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.math.BigInteger;

public final class StringToBigIntegerRuntimeConverter extends StringRuntimeConverter<BigInteger> {

    public StringToBigIntegerRuntimeConverter() {
        super(BigInteger.class);
    }

    @Override
    public BigInteger convert(String source, ConversionContext context) {
        return new BigInteger(source);
    }
}
