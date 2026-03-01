package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToShortConverter {

    @Test
    public void testConvertValidValues() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(Short.valueOf("123"));
        Assertions.assertThat(converter.convert("-123")).isEqualTo(Short.valueOf("-123"));
        Assertions.assertThat(converter.convert("0")).isEqualTo(Short.valueOf("0"));
    }

    @Test
    public void testConvertNull() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc"))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("123.45"))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertOutOfRange() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("32768"))
                .isInstanceOf(NumberFormatException.class);
    }
}
