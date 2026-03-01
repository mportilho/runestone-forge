package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToBooleanConverter {

    @Test
    public void testConvertValidValues() {
        StringToBooleanConverter converter = new StringToBooleanConverter();
        Assertions.assertThat(converter.convert("true")).isTrue();
        Assertions.assertThat(converter.convert("TRUE")).isTrue();
        Assertions.assertThat(converter.convert("on")).isTrue();
        Assertions.assertThat(converter.convert("ON")).isTrue();
        Assertions.assertThat(converter.convert("yes")).isTrue();
        Assertions.assertThat(converter.convert("YES")).isTrue();
        Assertions.assertThat(converter.convert("1")).isTrue();
        
        Assertions.assertThat(converter.convert("false")).isFalse();
        Assertions.assertThat(converter.convert("FALSE")).isFalse();
        Assertions.assertThat(converter.convert("0")).isFalse();
        Assertions.assertThat(converter.convert("")).isFalse();
        Assertions.assertThat(converter.convert("random")).isFalse();
    }

    @Test
    public void testConvertNull() {
        StringToBooleanConverter converter = new StringToBooleanConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
}
