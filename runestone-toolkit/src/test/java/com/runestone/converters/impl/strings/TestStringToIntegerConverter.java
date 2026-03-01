package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToIntegerConverter {

    @Test
    public void testConvertValidValues() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(Integer.valueOf("123"));
        Assertions.assertThat(converter.convert("-123")).isEqualTo(Integer.valueOf("-123"));
        Assertions.assertThat(converter.convert("0")).isEqualTo(Integer.valueOf("0"));
    }

    @Test
    public void testConvertNull() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc"))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("123.45"))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertOutOfRange() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("2147483648"))
                .isInstanceOf(NumberFormatException.class);
    }
}
