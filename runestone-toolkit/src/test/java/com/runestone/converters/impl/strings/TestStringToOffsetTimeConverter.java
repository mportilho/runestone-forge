package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToOffsetTimeConverter {

    @Test
    public void testConvertValidValues() {
        StringToOffsetTimeConverter converter = new StringToOffsetTimeConverter();
        Assertions.assertThat(converter.convert("00:00:00Z").toString())
                .isEqualTo("00:00Z");
    }

    @Test
    public void testConvertNull() {
        StringToOffsetTimeConverter converter = new StringToOffsetTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToOffsetTimeConverter converter = new StringToOffsetTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToOffsetTimeConverter converter = new StringToOffsetTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
