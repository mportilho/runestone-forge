package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class TestStringToTimestampConverter {

    @Test
    public void testConvertValidValues() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z", ConversionContext.standard())).isNotNull();
    }

    @Test
    public void testConvertTemporalRoutingBranches() {
        StringToTimestampConverter converter = new StringToTimestampConverter();

        Assertions.assertThat(converter.convert("1969-12-31", ConversionContext.standard()))
                .isEqualTo(Timestamp.from(LocalDate.of(1969, 12, 31).atStartOfDay(ConversionContext.standard().zoneId()).toInstant()));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15", ConversionContext.standard()))
                .isEqualTo(Timestamp.from(LocalDateTime.of(2021, 1, 1, 12, 30, 15)
                        .atZone(ConversionContext.standard().zoneId()).toInstant()));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15.123456789Z[UTC]", ConversionContext.standard()))
                .isEqualTo(Timestamp.from(Instant.parse("2021-01-01T12:30:15.123456789Z")));
    }

    @Test
    public void testLeapDayBoundaries() {
        StringToTimestampConverter converter = new StringToTimestampConverter();

        Assertions.assertThat(converter.convert("2020-02-29", ConversionContext.standard())).isNotNull();
        Assertions.assertThatThrownBy(() -> converter.convert("2019-02-29", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertNull() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }
}
