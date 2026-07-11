package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.format.DateTimeParseException;

public class TestStringToDurationConverter {

    @Test
    public void testConvertValidValues() {
        StringToDurationConverter converter = new StringToDurationConverter();
        Assertions.assertThat(converter.convert("PT15M", ConversionContext.standard())).isEqualTo(Duration.ofMinutes(15));
        Assertions.assertThat(converter.convert("P2DT3H4M", ConversionContext.standard())).isEqualTo(Duration.ofDays(2).plusHours(3).plusMinutes(4));
    }

    @Test
    public void testConvertNull() {
        StringToDurationConverter converter = new StringToDurationConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToDurationConverter converter = new StringToDurationConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("15 minutes", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }
}
