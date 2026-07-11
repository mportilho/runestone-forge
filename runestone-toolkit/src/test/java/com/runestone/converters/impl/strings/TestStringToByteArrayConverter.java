package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Base64;

public class TestStringToByteArrayConverter {

    @Test
    public void testConvertValidValues() {
        StringToByteArrayConverter converter = new StringToByteArrayConverter();
        byte[] expected = "test-string".getBytes();
        String encoded = Base64.getEncoder().encodeToString(expected);
        
        Assertions.assertThat(converter.convert(encoded, ConversionContext.standard())).isEqualTo(expected);
    }

    @Test
    public void testConvertNull() {
        StringToByteArrayConverter converter = new StringToByteArrayConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformedBase64() {
        StringToByteArrayConverter converter = new StringToByteArrayConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("invalid!base64", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
