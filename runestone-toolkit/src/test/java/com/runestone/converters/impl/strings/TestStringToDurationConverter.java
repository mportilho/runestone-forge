package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.format.DateTimeParseException;

public class TestStringToDurationConverter {

    @Test
    public void testConvertValidValues() {
        StringToDurationConverter converter = new StringToDurationConverter();
        Assertions.assertThat(converter.convert("PT15M")).isEqualTo(Duration.ofMinutes(15));
        Assertions.assertThat(converter.convert("P2DT3H4M")).isEqualTo(Duration.ofDays(2).plusHours(3).plusMinutes(4));
    }

    @Test
    public void testConvertNull() {
        StringToDurationConverter converter = new StringToDurationConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToDurationConverter converter = new StringToDurationConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("15 minutes"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
