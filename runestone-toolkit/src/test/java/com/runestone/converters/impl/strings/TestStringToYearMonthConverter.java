package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToYearMonthConverter {

    @Test
    public void testConvertValidValues() {
        StringToYearMonthConverter converter = new StringToYearMonthConverter();
        Assertions.assertThat(converter.convert("2021-01", ConversionContext.standard()).toString())
                .isEqualTo("2021-01");
    }

    @Test
    public void testConvertNull() {
        StringToYearMonthConverter converter = new StringToYearMonthConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToYearMonthConverter converter = new StringToYearMonthConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToYearMonthConverter converter = new StringToYearMonthConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }
}
