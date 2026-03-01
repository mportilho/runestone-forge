package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeParseException;

public class TestStringToJavaUtilDateConverter {

    @Test
    public void testConvertValidValues() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01")).isNotNull();
    }

    @Test
    public void testConvertNull() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
