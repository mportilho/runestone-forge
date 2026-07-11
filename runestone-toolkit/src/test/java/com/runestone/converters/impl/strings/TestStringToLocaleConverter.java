package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;

public class TestStringToLocaleConverter {

    @Test
    public void testConvertValidValues() {
        StringToLocaleConverter converter = new StringToLocaleConverter();
        Assertions.assertThat(converter.convert("pt-BR", ConversionContext.standard())).isEqualTo(Locale.forLanguageTag("pt-BR"));
        Assertions.assertThat(converter.convert("en-US", ConversionContext.standard())).isEqualTo(Locale.forLanguageTag("en-US"));
        Assertions.assertThat(converter.convert("fr", ConversionContext.standard())).isEqualTo(Locale.forLanguageTag("fr"));
        Assertions.assertThat(converter.convert("zh-Hant-TW", ConversionContext.standard())).isEqualTo(Locale.forLanguageTag("zh-Hant-TW"));

        // Handling legacy format with underscore
        Assertions.assertThat(converter.convert("pt_BR", ConversionContext.standard())).isEqualTo(Locale.forLanguageTag("pt-BR"));
        Assertions.assertThat(converter.convert("zh_Hant_TW", ConversionContext.standard())).isEqualTo(Locale.forLanguageTag("zh-Hant-TW"));
    }

    @Test
    public void testConvertNull() {
        StringToLocaleConverter converter = new StringToLocaleConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }
    
    @Test
    public void testConvertEmptyBlankAndInvalid() {
        StringToLocaleConverter converter = new StringToLocaleConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Locale language tag must not be blank");
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Locale language tag must not be blank");
        Assertions.assertThatThrownBy(() -> converter.convert("not a locale", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid locale language tag: not a locale");
    }
}
