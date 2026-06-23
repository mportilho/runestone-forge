package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToLocalDateTimeConverter {

    @Test
    public void testConvertValidValues() {
        StringToLocalDateTimeConverter converter = new StringToLocalDateTimeConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00").toString())
                .isEqualTo("2021-01-01T00:00");
    }

    @Test
    public void testConvertNull() {
        StringToLocalDateTimeConverter converter = new StringToLocalDateTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToLocalDateTimeConverter converter = new StringToLocalDateTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToLocalDateTimeConverter converter = new StringToLocalDateTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
