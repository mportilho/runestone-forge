package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.PatternSyntaxException;

public class TestStringToPatternConverter {

    @Test
    public void testConvertValidValues() {
        StringToPatternConverter converter = new StringToPatternConverter();
        Assertions.assertThat(converter.convert("^[a-z]+$").pattern()).isEqualTo("^[a-z]+$");
    }

    @Test
    public void testConvertNull() {
        StringToPatternConverter converter = new StringToPatternConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToPatternConverter converter = new StringToPatternConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("[unclosed bracket"))
                .isInstanceOf(PatternSyntaxException.class);
    }
}
