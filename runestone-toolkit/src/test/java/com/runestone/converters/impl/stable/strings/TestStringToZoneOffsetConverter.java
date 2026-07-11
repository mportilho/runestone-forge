package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.DateTimeException;

public class TestStringToZoneOffsetConverter {

    @Test
    public void testConvertValidValues() {
        StringToZoneOffsetConverter converter = new StringToZoneOffsetConverter();
        Assertions.assertThat(converter.convert("-03:00", ConversionContext.standard())).isEqualTo(ZoneOffset.of("-03:00"));
        Assertions.assertThat(converter.convert("Z", ConversionContext.standard())).isEqualTo(ZoneOffset.UTC);
        Assertions.assertThat(converter.convert("+02:00", ConversionContext.standard())).isEqualTo(ZoneOffset.ofHours(2));
    }

    @Test
    public void testConvertNull() {
        StringToZoneOffsetConverter converter = new StringToZoneOffsetConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToZoneOffsetConverter converter = new StringToZoneOffsetConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("MST", ConversionContext.standard())) // MST is ZoneId, not Offset
                .isInstanceOf(DateTimeException.class);
    }
}
