package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToDoubleConverter {

    @Test
    public void testConvertValidValues() {
        StringToDoubleConverter converter = new StringToDoubleConverter();
        Assertions.assertThat(converter.convert("123.45", ConversionContext.standard())).isEqualTo(Double.valueOf("123.45"));
        Assertions.assertThat(converter.convert("-123.45", ConversionContext.standard())).isEqualTo(Double.valueOf("-123.45"));
        Assertions.assertThat(converter.convert("0.0", ConversionContext.standard())).isEqualTo(Double.valueOf("0.0"));
        Assertions.assertThat(converter.convert("NaN", ConversionContext.standard())).isEqualTo(Double.valueOf("NaN"));
        Assertions.assertThat(converter.convert("Infinity", ConversionContext.standard())).isEqualTo(Double.valueOf("Infinity"));
        Assertions.assertThat(converter.convert("-Infinity", ConversionContext.standard())).isEqualTo(Double.valueOf("-Infinity"));
    }

    @Test
    public void testConvertNull() {
        StringToDoubleConverter converter = new StringToDoubleConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToDoubleConverter converter = new StringToDoubleConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToDoubleConverter converter = new StringToDoubleConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }
}
