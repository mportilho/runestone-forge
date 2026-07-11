package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class TestStringToUUIDConverter {

    @Test
    public void testConvertValidValues() {
        StringToUUIDConverter converter = new StringToUUIDConverter();
        String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
        Assertions.assertThat(converter.convert(uuidStr, ConversionContext.standard())).isEqualTo(UUID.fromString(uuidStr));
    }

    @Test
    public void testConvertNull() {
        StringToUUIDConverter converter = new StringToUUIDConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToUUIDConverter converter = new StringToUUIDConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("invalid-uuid-string", ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
