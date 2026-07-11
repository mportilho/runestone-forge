package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToLongConverter {

    @Test
    public void testConvertValidValues() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThat(converter.convert("123", ConversionContext.standard())).isEqualTo(Long.valueOf("123"));
        Assertions.assertThat(converter.convert("-123", ConversionContext.standard())).isEqualTo(Long.valueOf("-123"));
        Assertions.assertThat(converter.convert("0", ConversionContext.standard())).isEqualTo(Long.valueOf("0"));
    }

    @Test
    public void testConvertNull() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("123.45", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertOutOfRange() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("9223372036854775808", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }
}
