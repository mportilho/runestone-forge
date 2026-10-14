package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class TestStringToURIConverter {

    @Test
    public void testConvertValidValues() {
        StringToURIConverter converter = new StringToURIConverter();
        Assertions.assertThat(converter.convert("https://example.com", ConversionContext.standard())).isEqualTo(URI.create("https://example.com"));
        Assertions.assertThat(converter.convert("urn:isbn:096139210x", ConversionContext.standard())).isEqualTo(URI.create("urn:isbn:096139210x"));
    }

    @Test
    public void testConvertNull() {
        StringToURIConverter converter = new StringToURIConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToURIConverter converter = new StringToURIConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("https://exa mple.com", ConversionContext.standard())) // Space breaks URI syntax
                .isInstanceOf(IllegalArgumentException.class);
    }
}
