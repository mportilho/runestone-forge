package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToByteConverter {

    @Test
    public void testConvertValidValues() {
        StringToByteConverter converter = new StringToByteConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(Byte.valueOf("123"));
        Assertions.assertThat(converter.convert("-123")).isEqualTo(Byte.valueOf("-123"));
        Assertions.assertThat(converter.convert("0")).isEqualTo(Byte.valueOf("0"));
    }

    @Test
    public void testConvertNull() {
        StringToByteConverter converter = new StringToByteConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToByteConverter converter = new StringToByteConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToByteConverter converter = new StringToByteConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc"))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertOutOfRange() {
        StringToByteConverter converter = new StringToByteConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("128"))
                .isInstanceOf(NumberFormatException.class);
    }
}
