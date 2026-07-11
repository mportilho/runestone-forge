package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.zone.ZoneRulesException;

public class TestStringToZoneIdConverter {

    @Test
    public void testConvertValidValues() {
        StringToZoneIdConverter converter = new StringToZoneIdConverter();
        Assertions.assertThat(converter.convert("America/Sao_Paulo", ConversionContext.standard())).isEqualTo(ZoneId.of("America/Sao_Paulo"));
        Assertions.assertThat(converter.convert("UTC", ConversionContext.standard())).isEqualTo(ZoneId.of("UTC"));
    }

    @Test
    public void testConvertNull() {
        StringToZoneIdConverter converter = new StringToZoneIdConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertUnknownZone() {
        StringToZoneIdConverter converter = new StringToZoneIdConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("Unknown/Region", ConversionContext.standard()))
                .isInstanceOf(ZoneRulesException.class);
    }
}
