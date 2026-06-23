package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class TestStringToURIConverter {

    @Test
    public void testConvertValidValues() {
        StringToURIConverter converter = new StringToURIConverter();
        Assertions.assertThat(converter.convert("https://example.com")).isEqualTo(URI.create("https://example.com"));
        Assertions.assertThat(converter.convert("urn:isbn:096139210x")).isEqualTo(URI.create("urn:isbn:096139210x"));
    }

    @Test
    public void testConvertNull() {
        StringToURIConverter converter = new StringToURIConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToURIConverter converter = new StringToURIConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("https://exa mple.com")) // Space breaks URI syntax
                .isInstanceOf(IllegalArgumentException.class);
    }
}
