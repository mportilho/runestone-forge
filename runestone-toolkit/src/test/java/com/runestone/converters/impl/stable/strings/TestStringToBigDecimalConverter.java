package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestStringToBigDecimalConverter {

    @Test
    public void testConvertValidValues() {
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        Assertions.assertThat(converter.convert("123.45", ConversionContext.standard())).isEqualTo(new BigDecimal("123.45"));
        Assertions.assertThat(converter.convert("-123.45", ConversionContext.standard())).isEqualTo(new BigDecimal("-123.45"));
        Assertions.assertThat(converter.convert("0", ConversionContext.standard())).isEqualTo(new BigDecimal("0"));
    }

    @Test
    public void testConvertNull() {
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc", ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }
}
