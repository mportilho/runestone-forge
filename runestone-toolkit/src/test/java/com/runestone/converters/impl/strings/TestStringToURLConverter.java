package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;

public class TestStringToURLConverter {

    @Test
    public void testConvertValidValues() throws MalformedURLException {
        StringToURLConverter converter = new StringToURLConverter();
        Assertions.assertThat(converter.convert("https://example.com", ConversionContext.standard()))
                .isEqualTo(URI.create("https://example.com").toURL());
    }

    @Test
    public void testConvertNull() {
        StringToURLConverter converter = new StringToURLConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformedSyntax() {
        StringToURLConverter converter = new StringToURLConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("https://exa mple.com", ConversionContext.standard())) // Space breaks URI syntax first
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConvertInvalidProtocol() {
        StringToURLConverter converter = new StringToURLConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("urn:isbn:096139210x", ConversionContext.standard())) // Valid URI, invalid URL
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid URL");
    }
}
