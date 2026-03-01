package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TestStringToCharsetConverter {

    @Test
    public void testConvertValidValues() {
        StringToCharsetConverter converter = new StringToCharsetConverter();
        Assertions.assertThat(converter.convert("UTF-8")).isEqualTo(StandardCharsets.UTF_8);
        Assertions.assertThat(converter.convert("ISO-8859-1")).isEqualTo(StandardCharsets.ISO_8859_1);
        Assertions.assertThat(converter.convert("US-ASCII")).isEqualTo(StandardCharsets.US_ASCII);
    }

    @Test
    public void testConvertNull() {
        StringToCharsetConverter converter = new StringToCharsetConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToCharsetConverter converter = new StringToCharsetConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("NOT-A-CHARSET"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NOT-A-CHARSET");
    }
}
