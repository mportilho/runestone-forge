package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToTimestampConverter {

    @Test
    public void testConvertValidValues() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z")).isNotNull();
    }

    @Test
    public void testConvertNull() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
