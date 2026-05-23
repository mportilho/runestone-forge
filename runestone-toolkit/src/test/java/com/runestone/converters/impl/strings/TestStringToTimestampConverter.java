package com.runestone.converters.impl.strings;

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
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z")).isNotNull();
    }

    @Test
    public void testConvertTemporalRoutingBranches() {
        StringToTimestampConverter converter = new StringToTimestampConverter();

        Assertions.assertThat(converter.convert("1969-12-31"))
                .isEqualTo(Timestamp.valueOf(LocalDate.of(1969, 12, 31).atStartOfDay()));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15"))
                .isEqualTo(Timestamp.valueOf(LocalDateTime.of(2021, 1, 1, 12, 30, 15)));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15.123456789Z[UTC]"))
                .isEqualTo(Timestamp.from(Instant.parse("2021-01-01T12:30:15.123456789Z")));
    }

    @Test
    public void testLeapDayBoundaries() {
        StringToTimestampConverter converter = new StringToTimestampConverter();

        Assertions.assertThat(converter.convert("2020-02-29")).isNotNull();
        Assertions.assertThatThrownBy(() -> converter.convert("2019-02-29"))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertNull() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
