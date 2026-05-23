package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

public class TestStringToJavaUtilDateConverter {

    @Test
    public void testConvertValidValues() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01")).isNotNull();
    }

    @Test
    public void testConvertTemporalRoutingBranchesWithControlledTimezone() {
        withDefaultTimeZone("America/Sao_Paulo", () -> {
            StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
            ZoneId zoneId = ZoneId.systemDefault();

            Assertions.assertThat(converter.convert("1969-12-31"))
                    .isEqualTo(Date.from(LocalDate.of(1969, 12, 31).atStartOfDay(zoneId).toInstant()));
            Assertions.assertThat(converter.convert("2021-01-01T12:30:15"))
                    .isEqualTo(Date.from(LocalDateTime.of(2021, 1, 1, 12, 30, 15).atZone(zoneId).toInstant()));
            Assertions.assertThat(converter.convert("2021-01-01T12:30:15.123456789Z[UTC]"))
                    .isEqualTo(Date.from(ZonedDateTime.parse("2021-01-01T12:30:15.123456789Z[UTC]").toInstant()));
        });
    }

    @Test
    public void testLeapDayBoundaries() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();

        Assertions.assertThat(converter.convert("2020-02-29")).isNotNull();
        Assertions.assertThatThrownBy(() -> converter.convert("2019-02-29"))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertNull() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }

    private static void withDefaultTimeZone(String zoneId, Runnable assertions) {
        TimeZone original = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
        try {
            assertions.run();
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
