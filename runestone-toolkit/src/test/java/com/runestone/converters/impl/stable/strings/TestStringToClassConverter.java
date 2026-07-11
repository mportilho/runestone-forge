package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToClassConverter {

    @Test
    public void testConvertValidValues() {
        StringToClassConverter converter = new StringToClassConverter();
        Assertions.assertThat(converter.convert("java.lang.String", ConversionContext.standard())).isEqualTo(String.class);
    }

    @Test
    public void testConvertNull() {
        StringToClassConverter converter = new StringToClassConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class); // Class.forName(null) throws NPE or within try-catch throws NPE on `data`
    }

    @Test
    public void testConvertMalformed() {
        StringToClassConverter converter = new StringToClassConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("com.unknown.FakeClass", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Class not found");
    }
}
