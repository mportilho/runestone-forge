package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public final class StringToURLRuntimeConverter extends StringRuntimeConverter<URL> {

    public StringToURLRuntimeConverter() {
        super(URL.class);
    }

    @Override
    public URL convert(String source, ConversionContext context) {
        try {
            return URI.create(source).toURL();
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Invalid URL: " + source, exception);
        }
    }
}
