package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestStringToBooleanConverter {

    @Test
    public void testConvertValidValues() {
        StringToBooleanConverter converter = new StringToBooleanConverter();
        Assertions.assertThat(converter.convert("true", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert("TRUE", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert("TrUe", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert(" true ", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert("on", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert("ON", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert("yes", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert("YES", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert("YeS", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert(" yes ", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert("1", ConversionContext.standard())).isTrue();
        Assertions.assertThat(converter.convert(" 1 ", ConversionContext.standard())).isTrue();
        
        Assertions.assertThat(converter.convert("false", ConversionContext.standard())).isFalse();
        Assertions.assertThat(converter.convert("FALSE", ConversionContext.standard())).isFalse();
        Assertions.assertThat(converter.convert("0", ConversionContext.standard())).isFalse();
        Assertions.assertThat(converter.convert("", ConversionContext.standard())).isFalse();
        Assertions.assertThat(converter.convert("   ", ConversionContext.standard())).isFalse();
        Assertions.assertThat(converter.convert("random", ConversionContext.standard())).isFalse();
    }

    @Test
    public void testConvertNull() {
        StringToBooleanConverter converter = new StringToBooleanConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }
}
