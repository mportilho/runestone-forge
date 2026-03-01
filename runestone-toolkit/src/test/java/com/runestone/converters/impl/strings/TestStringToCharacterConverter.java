package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToCharacterConverter {

    @Test
    public void testConvertValidValues() {
        StringToCharacterConverter converter = new StringToCharacterConverter();
        Assertions.assertThat(converter.convert("a")).isEqualTo('a');
        Assertions.assertThat(converter.convert("A")).isEqualTo('A');
        
        // Takes only the first char
        Assertions.assertThat(converter.convert("abc")).isEqualTo('a');
    }

    @Test
    public void testConvertNull() {
        StringToCharacterConverter converter = new StringToCharacterConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testConvertEmpty() {
        StringToCharacterConverter converter = new StringToCharacterConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("String must not be empty");
    }
}
