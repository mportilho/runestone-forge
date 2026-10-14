package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class TestStringToZonedDateTimeConverter {

    @Test
    public void testConvertValidValues() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z", ConversionContext.standard()).toString())
                .isEqualTo("2021-01-01T00:00Z");
    }

    @Test
    public void testConvertValuesWithoutZoneUsingContextTimezone() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        ConversionContext context = new ConversionContext(zoneId, Locale.ROOT);

        Assertions.assertThat(converter.convert("2021-01-01", context))
                .isEqualTo(ZonedDateTime.of(2021, 1, 1, 0, 0, 0, 0, zoneId));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15", context))
                .isEqualTo(ZonedDateTime.of(2021, 1, 1, 12, 30, 15, 0, zoneId));
    }

    @Test
    public void testConvertNull() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }
}
