package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.PatternSyntaxException;

public class TestStringToPatternConverter {

    @Test
    public void testConvertValidValues() {
        StringToPatternConverter converter = new StringToPatternConverter();
        Assertions.assertThat(converter.convert("^[a-z]+$", ConversionContext.standard()).pattern()).isEqualTo("^[a-z]+$");
    }

    @Test
    public void testConvertNull() {
        StringToPatternConverter converter = new StringToPatternConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToPatternConverter converter = new StringToPatternConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("[unclosed bracket", ConversionContext.standard()))
                .isInstanceOf(PatternSyntaxException.class);
    }
}
