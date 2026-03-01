package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneOffset;
import java.time.DateTimeException;

public class TestStringToZoneOffsetConverter {

    @Test
    public void testConvertValidValues() {
        StringToZoneOffsetConverter converter = new StringToZoneOffsetConverter();
        Assertions.assertThat(converter.convert("-03:00")).isEqualTo(ZoneOffset.of("-03:00"));
        Assertions.assertThat(converter.convert("Z")).isEqualTo(ZoneOffset.UTC);
        Assertions.assertThat(converter.convert("+02:00")).isEqualTo(ZoneOffset.ofHours(2));
    }

    @Test
    public void testConvertNull() {
        StringToZoneOffsetConverter converter = new StringToZoneOffsetConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToZoneOffsetConverter converter = new StringToZoneOffsetConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("MST")) // MST is ZoneId, not Offset
                .isInstanceOf(DateTimeException.class);
    }
}
