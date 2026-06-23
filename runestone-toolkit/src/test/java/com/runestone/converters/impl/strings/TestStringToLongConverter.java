package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToLongConverter {

    @Test
    public void testConvertValidValues() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(Long.valueOf("123"));
        Assertions.assertThat(converter.convert("-123")).isEqualTo(Long.valueOf("-123"));
        Assertions.assertThat(converter.convert("0")).isEqualTo(Long.valueOf("0"));
    }

    @Test
    public void testConvertNull() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc"))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("123.45"))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertOutOfRange() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("9223372036854775808"))
                .isInstanceOf(NumberFormatException.class);
    }
}
