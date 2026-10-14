package com.runestone.converters.impl.runtime.strings;

import com.runestone.converters.ConversionContext;

import java.net.URI;

public final class StringToURIRuntimeConverter extends StringRuntimeConverter<URI> {

    public StringToURIRuntimeConverter() {
        super(URI.class);
    }

    @Override
    public URI convert(String source, ConversionContext context) {
        return URI.create(source);
    }
}
