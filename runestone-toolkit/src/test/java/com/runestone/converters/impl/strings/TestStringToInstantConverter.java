package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToInstantConverter {

    @Test
    public void testConvertValidValues() {
        StringToInstantConverter converter = new StringToInstantConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z").toString())
                .isEqualTo("2021-01-01T00:00:00Z");
    }

    @Test
    public void testConvertNull() {
        StringToInstantConverter converter = new StringToInstantConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToInstantConverter converter = new StringToInstantConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToInstantConverter converter = new StringToInstantConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
