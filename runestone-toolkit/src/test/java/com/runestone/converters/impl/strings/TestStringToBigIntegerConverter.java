package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class TestStringToBigIntegerConverter {

    @Test
    public void testConvertValidValues() {
        StringToBigIntegerConverter converter = new StringToBigIntegerConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(new BigInteger("123"));
        Assertions.assertThat(converter.convert("-123")).isEqualTo(new BigInteger("-123"));
        Assertions.assertThat(converter.convert("0")).isEqualTo(new BigInteger("0"));
    }

    @Test
    public void testConvertNull() {
        StringToBigIntegerConverter converter = new StringToBigIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToBigIntegerConverter converter = new StringToBigIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(NumberFormatException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToBigIntegerConverter converter = new StringToBigIntegerConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("abc"))
                .isInstanceOf(NumberFormatException.class);
    }
}
