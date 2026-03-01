package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Currency;

public class TestStringToCurrencyConverter {

    @Test
    public void testConvertValidValues() {
        StringToCurrencyConverter converter = new StringToCurrencyConverter();
        Assertions.assertThat(converter.convert("BRL")).isEqualTo(Currency.getInstance("BRL"));
        Assertions.assertThat(converter.convert("USD")).isEqualTo(Currency.getInstance("USD"));
        Assertions.assertThat(converter.convert("EUR")).isEqualTo(Currency.getInstance("EUR"));
    }

    @Test
    public void testConvertNull() {
        StringToCurrencyConverter converter = new StringToCurrencyConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToCurrencyConverter converter = new StringToCurrencyConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("unknownCurrency"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
