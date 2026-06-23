package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

public class TestStringToBigDecimalConverter {

    @Test
    public void testConvertValidValues() {
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        Assertions.assertThat(converter.convert("123.45")).isEqualTo(new BigDecimal("123.45"));
        Assertions.assertThat(converter.convert("-123.45")).isEqualTo(new BigDecimal("-123.45"));
        Assertions.assertThat(converter.convert("0")).isEqualTo(new BigDecimal("0"));
    }

    @Test
    public void testConvertNull() {
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc"))
                .isInstanceOf(NumberFormatException.class);
    }
}
