package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TestStringToJavaSqlDateConverter {

    @Test
    public void testConvertValidValues() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01", ConversionContext.standard()).getTime())
                .isEqualTo(utcStartOfDay(LocalDate.of(2021, 1, 1)));
    }

    @Test
    public void testConvertTemporalRoutingBranches() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();

        Assertions.assertThat(converter.convert("1969-12-31", ConversionContext.standard()).getTime())
                .isEqualTo(utcStartOfDay(LocalDate.of(1969, 12, 31)));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15", ConversionContext.standard()).getTime())
                .isEqualTo(utcStartOfDay(LocalDate.of(2021, 1, 1)));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15.123456789Z[UTC]", ConversionContext.standard()).getTime())
                .isEqualTo(utcStartOfDay(LocalDate.of(2021, 1, 1)));
    }

    @Test
    public void testLeapDayBoundaries() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();

        Assertions.assertThat(converter.convert("2020-02-29", ConversionContext.standard()).getTime())
                .isEqualTo(utcStartOfDay(LocalDate.of(2020, 2, 29)));
        Assertions.assertThatThrownBy(() -> converter.convert("2019-02-29", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertNull() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    private static long utcStartOfDay(LocalDate date) {
        return date.atStartOfDay(ConversionContext.standard().zoneId()).toInstant().toEpochMilli();
    }
}
