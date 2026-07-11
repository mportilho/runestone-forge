package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToShortConverter {

    @Test
    public void testConvertValidValues() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThat(converter.convert("123", ConversionContext.standard())).isEqualTo(Short.valueOf("123"));
        Assertions.assertThat(converter.convert("-123", ConversionContext.standard())).isEqualTo(Short.valueOf("-123"));
        Assertions.assertThat(converter.convert("0", ConversionContext.standard())).isEqualTo(Short.valueOf("0"));
    }

    @Test
    public void testConvertNull() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("123.45", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertOutOfRange() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("32768", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }
}
