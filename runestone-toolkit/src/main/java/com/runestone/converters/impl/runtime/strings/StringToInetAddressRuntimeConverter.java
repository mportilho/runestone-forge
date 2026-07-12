package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class StringToInetAddressRuntimeConverter extends StringRuntimeConverter<InetAddress> {

    public StringToInetAddressRuntimeConverter() {
        super(InetAddress.class);
    }

    @Override
    public InetAddress convert(String source, ConversionContext context) {
        try {
            return InetAddress.getByName(source);
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException("Unknown host: " + source, exception);
        }
    }
}
