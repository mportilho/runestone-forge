package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToLocalTimeConverter {

    @Test
    public void testConvertValidValues() {
        StringToLocalTimeConverter converter = new StringToLocalTimeConverter();
        Assertions.assertThat(converter.convert("00:00:00", ConversionContext.standard()).toString())
                .isEqualTo("00:00");
    }

    @Test
    public void testConvertNull() {
        StringToLocalTimeConverter converter = new StringToLocalTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToLocalTimeConverter converter = new StringToLocalTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToLocalTimeConverter converter = new StringToLocalTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }
}
