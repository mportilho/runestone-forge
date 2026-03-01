package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

public class TestStringToLocaleConverter {

    @Test
    public void testConvertValidValues() {
        StringToLocaleConverter converter = new StringToLocaleConverter();
        Assertions.assertThat(converter.convert("pt-BR")).isEqualTo(Locale.forLanguageTag("pt-BR"));
        Assertions.assertThat(converter.convert("en-US")).isEqualTo(Locale.forLanguageTag("en-US"));
        Assertions.assertThat(converter.convert("fr")).isEqualTo(Locale.forLanguageTag("fr"));
        
        // Handling legacy format with underscore
        Assertions.assertThat(converter.convert("pt_BR")).isEqualTo(Locale.forLanguageTag("pt-BR"));
    }

    @Test
    public void testConvertNull() {
        StringToLocaleConverter converter = new StringToLocaleConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    public void testConvertEmpty() {
        StringToLocaleConverter converter = new StringToLocaleConverter();
        Assertions.assertThat(converter.convert("")).isEqualTo(Locale.ROOT);
    }
}
