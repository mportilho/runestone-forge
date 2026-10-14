package com.runestone.converters.impl.stable.strings;

import com.runestone.converters.ConversionContext;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

public class TestStringToJavaUtilDateConverter {

    @Test
    public void testConvertValidValues() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01", ConversionContext.standard())).isNotNull();
    }

    @Test
    public void testConvertTemporalRoutingBranchesWithContextTimezone() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        ZoneId zoneId = ZoneId.of("America/Sao_Paulo");
        ConversionContext context = new ConversionContext(zoneId, Locale.ROOT);

        Assertions.assertThat(converter.convert("1969-12-31", context))
                .isEqualTo(Date.from(LocalDate.of(1969, 12, 31).atStartOfDay(zoneId).toInstant()));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15", context))
                .isEqualTo(Date.from(LocalDateTime.of(2021, 1, 1, 12, 30, 15).atZone(zoneId).toInstant()));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15.123456789Z[UTC]", context))
                .isEqualTo(Date.from(ZonedDateTime.parse("2021-01-01T12:30:15.123456789Z[UTC]").toInstant()));
    }

    @Test
    public void testLeapDayBoundaries() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();

        Assertions.assertThat(converter.convert("2020-02-29", ConversionContext.standard())).isNotNull();
        Assertions.assertThatThrownBy(() -> converter.convert("2019-02-29", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertNull() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null, ConversionContext.standard()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   ", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date", ConversionContext.standard()))
                .isInstanceOf(DateTimeParseException.class);
    }
}
