package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToCharacterConverter {

    @Test
    public void testConvertValidValues() {
        StringToCharacterConverter converter = new StringToCharacterConverter();
        Assertions.assertThat(converter.convert("a", ConversionContext.standard())).isEqualTo('a');
        Assertions.assertThat(converter.convert("A", ConversionContext.standard())).isEqualTo('A');
        
        // Takes only the first char
        Assertions.assertThat(converter.convert("abc", ConversionContext.standard())).isEqualTo('a');
    }

    @Test
    public void testConvertNull() {
        StringToCharacterConverter converter = new StringToCharacterConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConvertEmpty() {
        StringToCharacterConverter converter = new StringToCharacterConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("String must not be empty");
    }
}
