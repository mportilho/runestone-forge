package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToIntegerConverter {

    @Test
    public void testConvertValidValues() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThat(converter.convert("123", ConversionContext.standard())).isEqualTo(Integer.valueOf("123"));
        Assertions.assertThat(converter.convert("-123", ConversionContext.standard())).isEqualTo(Integer.valueOf("-123"));
        Assertions.assertThat(converter.convert("0", ConversionContext.standard())).isEqualTo(Integer.valueOf("0"));
    }

    @Test
    public void testConvertNull() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("123.45", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertOutOfRange() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("2147483648", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }
}
