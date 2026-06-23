package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToZonedDateTimeConverter {

    @Test
    public void testConvertValidValues() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z").toString())
                .isEqualTo("2021-01-01T00:00Z");
    }

    @Test
    public void testConvertNull() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
