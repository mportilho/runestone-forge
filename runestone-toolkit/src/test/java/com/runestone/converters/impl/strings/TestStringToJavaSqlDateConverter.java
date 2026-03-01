package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToJavaSqlDateConverter {

    @Test
    public void testConvertValidValues() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01").toString())
                .isEqualTo("2021-01-01");
    }

    @Test
    public void testConvertNull() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
