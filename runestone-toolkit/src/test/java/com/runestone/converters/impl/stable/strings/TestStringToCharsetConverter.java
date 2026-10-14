package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TestStringToCharsetConverter {

    @Test
    public void testConvertValidValues() {
        StringToCharsetConverter converter = new StringToCharsetConverter();
        Assertions.assertThat(converter.convert("UTF-8", ConversionContext.standard())).isEqualTo(StandardCharsets.UTF_8);
        Assertions.assertThat(converter.convert("ISO-8859-1", ConversionContext.standard())).isEqualTo(StandardCharsets.ISO_8859_1);
        Assertions.assertThat(converter.convert("US-ASCII", ConversionContext.standard())).isEqualTo(StandardCharsets.US_ASCII);
    }

    @Test
    public void testConvertNull() {
        StringToCharsetConverter converter = new StringToCharsetConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToCharsetConverter converter = new StringToCharsetConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("NOT-A-CHARSET", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NOT-A-CHARSET");
    }
}
