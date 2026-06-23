package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToDoubleConverter {

    @Test
    public void testConvertValidValues() {
        StringToDoubleConverter converter = new StringToDoubleConverter();
        Assertions.assertThat(converter.convert("123.45")).isEqualTo(Double.valueOf("123.45"));
        Assertions.assertThat(converter.convert("-123.45")).isEqualTo(Double.valueOf("-123.45"));
        Assertions.assertThat(converter.convert("0.0")).isEqualTo(Double.valueOf("0.0"));
        Assertions.assertThat(converter.convert("NaN")).isEqualTo(Double.valueOf("NaN"));
        Assertions.assertThat(converter.convert("Infinity")).isEqualTo(Double.valueOf("Infinity"));
        Assertions.assertThat(converter.convert("-Infinity")).isEqualTo(Double.valueOf("-Infinity"));
    }

    @Test
    public void testConvertNull() {
        StringToDoubleConverter converter = new StringToDoubleConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToDoubleConverter converter = new StringToDoubleConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToDoubleConverter converter = new StringToDoubleConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc"))
                .isInstanceOf(NumberFormatException.class);
    }
}
