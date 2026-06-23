package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToClassConverter {

    @Test
    public void testConvertValidValues() {
        StringToClassConverter converter = new StringToClassConverter();
        Assertions.assertThat(converter.convert("java.lang.String")).isEqualTo(String.class);
    }

    @Test
    public void testConvertNull() {
        StringToClassConverter converter = new StringToClassConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class); // Class.forName(null) throws NPE or within try-catch throws NPE on `data`
    }

    @Test
    public void testConvertMalformed() {
        StringToClassConverter converter = new StringToClassConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("com.unknown.FakeClass"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Class not found");
    }
}
