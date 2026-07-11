package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToFloatConverter {

    @Test
    public void testConvertValidValues() {
        StringToFloatConverter converter = new StringToFloatConverter();
        Assertions.assertThat(converter.convert("123.45", ConversionContext.standard())).isEqualTo(Float.valueOf("123.45"));
        Assertions.assertThat(converter.convert("-123.45", ConversionContext.standard())).isEqualTo(Float.valueOf("-123.45"));
        Assertions.assertThat(converter.convert("0.0", ConversionContext.standard())).isEqualTo(Float.valueOf("0.0"));
        Assertions.assertThat(converter.convert("NaN", ConversionContext.standard())).isEqualTo(Float.valueOf("NaN"));
        Assertions.assertThat(converter.convert("Infinity", ConversionContext.standard())).isEqualTo(Float.valueOf("Infinity"));
        Assertions.assertThat(converter.convert("-Infinity", ConversionContext.standard())).isEqualTo(Float.valueOf("-Infinity"));
    }

    @Test
    public void testConvertNull() {
        StringToFloatConverter converter = new StringToFloatConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToFloatConverter converter = new StringToFloatConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToFloatConverter converter = new StringToFloatConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }
}
