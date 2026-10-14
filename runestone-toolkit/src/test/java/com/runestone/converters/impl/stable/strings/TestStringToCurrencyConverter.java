package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Currency;

public class TestStringToCurrencyConverter {

    @Test
    public void testConvertValidValues() {
        StringToCurrencyConverter converter = new StringToCurrencyConverter();
        Assertions.assertThat(converter.convert("BRL", ConversionContext.standard())).isEqualTo(Currency.getInstance("BRL"));
        Assertions.assertThat(converter.convert("USD", ConversionContext.standard())).isEqualTo(Currency.getInstance("USD"));
        Assertions.assertThat(converter.convert("EUR", ConversionContext.standard())).isEqualTo(Currency.getInstance("EUR"));
    }

    @Test
    public void testConvertNull() {
        StringToCurrencyConverter converter = new StringToCurrencyConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToCurrencyConverter converter = new StringToCurrencyConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("unknownCurrency", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
