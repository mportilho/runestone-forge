package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

public class DateConverterBoundaryTest {

    @Test
    public void testUtilDateConvertersUseContextTimezoneForEpochAndPreEpochInstants() {
        ZoneId zoneId = ZoneId.of("America/New_York");
        ConversionContext context = new ConversionContext(zoneId, Locale.ROOT);
        java.util.Date epoch = java.util.Date.from(Instant.EPOCH);
        java.util.Date preEpoch = java.util.Date.from(Instant.parse("1960-01-01T03:30:00Z"));

        Assertions.assertThat(new UtilDateToLocalDateConverter().convert(epoch, context))
                .isEqualTo(LocalDate.of(1969, 12, 31));
        Assertions.assertThat(new UtilDateToLocalDateTimeConverter().convert(preEpoch, context))
                .isEqualTo(LocalDateTime.of(1959, 12, 31, 22, 30));
        Assertions.assertThat(new UtilDateToLocalTimeConverter().convert(preEpoch, context))
                .isEqualTo(LocalTime.of(22, 30));
        Assertions.assertThat(new UtilDateToZonedDateTimeConverter().convert(preEpoch, context))
                .isEqualTo(ZonedDateTime.of(LocalDateTime.of(1959, 12, 31, 22, 30), zoneId));
    }

    @Test
    public void testUtilDateConvertersHandleDstGapInstant() {
        ZoneId zoneId = ZoneId.of("America/New_York");
        ConversionContext context = new ConversionContext(zoneId, Locale.ROOT);
        java.util.Date date = java.util.Date.from(Instant.parse("2021-03-14T07:30:00Z"));

        Assertions.assertThat(new UtilDateToLocalDateTimeConverter().convert(date, context))
                .isEqualTo(LocalDateTime.of(2021, 3, 14, 3, 30));
        Assertions.assertThat(new UtilDateToZonedDateTimeConverter().convert(date, context))
                .isEqualTo(ZonedDateTime.of(LocalDateTime.of(2021, 3, 14, 3, 30), zoneId));
    }

    @Test
    public void testSqlDateConvertersUseContextTimezoneForNonMidnightMillis() {
        ZoneId zoneId = ZoneId.of("America/New_York");
        ConversionContext context = new ConversionContext(zoneId, Locale.ROOT);
        java.sql.Date sqlDate = new java.sql.Date(Instant.parse("2021-03-14T07:30:00Z").toEpochMilli());

        Assertions.assertThat(new SqlDateToLocalDateConverter().convert(sqlDate, context))
                .isEqualTo(LocalDate.of(2021, 3, 14));
        Assertions.assertThat(new SqlDateToLocalDateTimeConverter().convert(sqlDate, context))
                .isEqualTo(LocalDateTime.of(2021, 3, 14, 3, 30));
        Assertions.assertThat(new SqlDateToLocalTimeConverter().convert(sqlDate, context))
                .isEqualTo(LocalTime.of(3, 30));
        Assertions.assertThat(new SqlDateToZonedDateTimeConverter().convert(sqlDate, context))
                .isEqualTo(ZonedDateTime.of(LocalDateTime.of(2021, 3, 14, 3, 30), zoneId));
    }
}
