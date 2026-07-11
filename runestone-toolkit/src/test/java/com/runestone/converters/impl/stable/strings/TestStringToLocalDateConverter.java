package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToLocalDateConverter {

    @Test
    public void testConvertValidValues() {
        StringToLocalDateConverter converter = new StringToLocalDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01", ConversionContext.standard()).toString())
                .isEqualTo("2021-01-01");
    }

    @Test
    public void testConvertNull() {
        StringToLocalDateConverter converter = new StringToLocalDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToLocalDateConverter converter = new StringToLocalDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToLocalDateConverter converter = new StringToLocalDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }
}
