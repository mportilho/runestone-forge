package com.runestone.utils;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDateUtils {

    @Test
    public void testYearMonthPatterns() {
        assertThat(DateUtils.YEAR_MONTH_FORMATTER.parse("2019-03", YearMonth::from)).isEqualTo(YearMonth.of(2019, 3));
        assertThat(DateUtils.YEAR_MONTH_FORMATTER.parse("2019/03", YearMonth::from)).isEqualTo(YearMonth.of(2019, 3));
        assertThat(DateUtils.YEAR_MONTH_FORMATTER.parse("201903", YearMonth::from)).isEqualTo(YearMonth.of(2019, 3));
        assertThat(DateUtils.YEAR_MONTH_FORMATTER.parse("03/2019", YearMonth::from)).isEqualTo(YearMonth.of(2019, 3));
        assertThat(DateUtils.YEAR_MONTH_FORMATTER.parse("03-2019", YearMonth::from)).isEqualTo(YearMonth.of(2019, 3));
    }

    @Test
    public void testLocalDatePatterns() {
        assertThat(DateUtils.DATE_FORMATTER.parse("20191223", LocalDate::from)).isEqualTo(LocalDate.of(2019, 12, 23));
        assertThat(DateUtils.DATE_FORMATTER.parse("2000/12/14", LocalDate::from)).isEqualTo(LocalDate.of(2000, 12, 14));
        assertThat(DateUtils.DATE_FORMATTER.parse("2000-12-14", LocalDate::from)).isEqualTo(LocalDate.of(2000, 12, 14));
        assertThat(DateUtils.DATE_FORMATTER.parse("14-12-2000", LocalDate::from)).isEqualTo(LocalDate.of(2000, 12, 14));
        assertThat(DateUtils.DATE_FORMATTER.parse("14/12/2000", LocalDate::from)).isEqualTo(LocalDate.of(2000, 12, 14));

        assertThat(DateUtils.DATETIME_FORMATTER.parse("20191223", LocalDate::from)).isEqualTo(LocalDate.of(2019, 12, 23));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000/12/14", LocalDate::from)).isEqualTo(LocalDate.of(2000, 12, 14));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000-12-14", LocalDate::from)).isEqualTo(LocalDate.of(2000, 12, 14));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("14-12-2000", LocalDate::from)).isEqualTo(LocalDate.of(2000, 12, 14));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("14/12/2000", LocalDate::from)).isEqualTo(LocalDate.of(2000, 12, 14));
    }

    @Test
    public void testLocalTimePatterns() {
        assertThat(DateUtils.TIME_FORMATTER.parse("15:22", LocalTime::from))
                .isEqualTo(LocalTime.of(15, 22, 0));

        assertThat(DateUtils.TIME_FORMATTER.parse("15:22:32", LocalTime::from))
                .isEqualTo(LocalTime.of(15, 22, 32));

        assertThat(DateUtils.TIME_FORMATTER.parse("15:22:32.356", LocalTime::from))
                .isEqualTo(LocalTime.of(15, 22, 32, 356000000));

        assertThat(DateUtils.TIME_FORMATTER.parse("15:22:32.879-03:00", LocalTime::from))
                .isEqualTo(LocalTime.of(15, 22, 32, 879000000));

        assertThat(DateUtils.TIME_FORMATTER.parse("10:22:11+00:00", LocalTime::from))
                .isEqualTo(LocalTime.of(10, 22, 11));
    }

    @Test
    public void testLocalDateTimePatterns() {
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2022-01-27T13:18:57.147118", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2022, 1, 27, 13, 18, 57, 147118000));

        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000/12/14 22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000/12/14-22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000/12/14-22:23", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 0));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000/12/14 22:23:24.123", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24, 123000000));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000-12-14 22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000-12-14 22:23:24.123", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24, 123000000));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("14-12-2000 22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("14-12-2000 22:23:24.123", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24, 123000000));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("14/12/2000 22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("14/12/2000 22:23:24.123", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24, 123000000));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("14/12/2000 22:23", LocalDateTime::from)).isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2011-12-03T10:15:30", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        assertThat(DateUtils.DATETIME_FORMATTER.parse("2018-07-11T14:33:56.338Z", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2018, 7, 11, 14, 33, 56, 338000000));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2018-07-11T14:33:56.338", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2018, 7, 11, 14, 33, 56, 338000000));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2022-04-14T15:01:46.454", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2022, 4, 14, 15, 1, 46, 454000000));

        assertThat(DateUtils.DATETIME_FORMATTER.parse("2022-01-25T23:09:55.102085600", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2022, 1, 25, 23, 9, 55, 102085600));

        assertThat(DateUtils.DATETIME_FORMATTER.parse("01/10/2020 08:00", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2020, 10, 1, 8, 0));
    }

    @Test
    public void testLocalDateTimePaddingTimePatterns() {
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2022-01-27T13:18:57.147118", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2022, 1, 27, 13, 18, 57, 147118000));

        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2000/12/14 22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2000/12/14-22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2000/12/14-22:23", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 0));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2000/12/14 22:23:24.123", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24, 123000000));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2000-12-14 22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2000-12-14 22:23:24.123", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24, 123000000));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("14-12-2000 22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("14-12-2000 22:23:24.123", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24, 123000000));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("14/12/2000 22:23:24", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("14/12/2000 22:23:24.123", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23, 24, 123000000));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("14/12/2000 22:23", LocalDateTime::from)).isEqualTo(LocalDateTime.of(2000, 12, 14, 22, 23));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2011-12-03T10:15:30", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2011, 12, 3, 10, 15, 30));

        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2018-07-11T14:33:56.338Z", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2018, 7, 11, 14, 33, 56, 338000000));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2018-07-11T14:33:56.338", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2018, 7, 11, 14, 33, 56, 338000000));
        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2022-04-14T15:01:46.454", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2022, 4, 14, 15, 1, 46, 454000000));

        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("2022-01-25T23:09:55.102085600", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2022, 1, 25, 23, 9, 55, 102085600));

        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("01/10/2020 08:00", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2020, 10, 1, 8, 0));

        assertThat(DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse("01/10/2020", LocalDateTime::from))
                .isEqualTo(LocalDateTime.of(2020, 10, 1, 0, 0));
    }

    @Test
    public void testOffsetTimePatterns() {
        assertThat(DateUtils.TIME_FORMATTER.parse("15:22:32.000-03:00", OffsetTime::from))
                .isEqualTo(OffsetTime.of(15, 22, 32, 0, ZoneOffset.ofHours(-3)));
        assertThat(DateUtils.TIME_FORMATTER.parse("15:22:32.356-03:00", OffsetTime::from))
                .isEqualTo(OffsetTime.of(15, 22, 32, 356000000, ZoneOffset.ofHours(-3)));
        assertThat(DateUtils.TIME_FORMATTER.parse("15:22:32.879-03:00", OffsetTime::from))
                .isEqualTo(OffsetTime.of(15, 22, 32, 879000000, ZoneOffset.ofHours(-3)));
        assertThat(DateUtils.TIME_FORMATTER.parse("10:22:11-03:00", OffsetTime::from))
                .isEqualTo(OffsetTime.of(10, 22, 11, 0, ZoneOffset.ofHours(-3)));
        assertThat(DateUtils.TIME_FORMATTER.parse("10:15+01:00", OffsetTime::from))
                .isEqualTo(OffsetTime.of(10, 15, 0, 0, ZoneOffset.ofHours(+1)));
        assertThat(DateUtils.TIME_FORMATTER.parse("10:15:30+01:00", OffsetTime::from))
                .isEqualTo(OffsetTime.of(10, 15, 30, 0, ZoneOffset.ofHours(+1)));
    }

    @Test
    public void testOffsetDateTimePatterns() {
        assertThat(DateUtils.DATETIME_FORMATTER.parse("14-12-2000 22:23:24.123-04:00", OffsetDateTime::from))
                .isEqualTo(OffsetDateTime.of(LocalDate.of(2000, 12, 14),
                        LocalTime.of(22, 23, 24, 123000000), ZoneOffset.ofHours(-4)));
    }

    @Test
    public void testZonedDateTimePatterns_WithOffset() {
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000/12/14 22:23:24.123-03:00", ZonedDateTime::from))
                .isEqualTo(ZonedDateTime.of(2000, 12, 14, 22, 23, 24, 123000000, ZoneOffset.ofHours(-3)));

        assertThat(DateUtils.DATETIME_FORMATTER.parse("2000-12-14 22:23:24.123 -0300", ZonedDateTime::from))
                .isEqualTo(ZonedDateTime.of(2000, 12, 14, 22, 23, 24, 123000000, ZoneOffset.ofHours(-3)));

        assertThat(DateUtils.DATETIME_FORMATTER.parse("14-12-2000 22:23:24.123 -0300", ZonedDateTime::from))
                .isEqualTo(ZonedDateTime.of(2000, 12, 14, 22, 23, 24, 123000000, ZoneOffset.ofHours(-3)));

        assertThat(DateUtils.DATETIME_FORMATTER.parse("14/12/2000 22:23:24.123 -0300", ZonedDateTime::from))
                .isEqualTo(ZonedDateTime.of(2000, 12, 14, 22, 23, 24, 123000000, ZoneOffset.ofHours(-3)));
        assertThat(DateUtils.DATETIME_FORMATTER.parse("2020-09-25T12:56:07.437-03:00", ZonedDateTime::from))
                .isEqualTo(ZonedDateTime.of(2020, 9, 25, 12, 56, 7, 437000000, ZoneOffset.ofHours(-3)));
    }

    @Test
    public void testZonedDateTimePatterns_WithZoneId() {
        assertThat(DateUtils.DATETIME_FORMATTER.parse(
                "2011-12-03T10:15:30+01:00[Europe/Paris]", ZonedDateTime::from)).isEqualTo(
                ZonedDateTime.of(2011, 12, 3, 10, 15, 30, 0, ZoneId.of("Europe/Paris")));
    }


    @Test
    public void testSpecialCaseTimeFormat() {
        assertThat(DateUtils.DATETIME_FORMATTER.parse("10:15:30+01:00", OffsetTime::from))
                .isEqualTo(OffsetTime.of(10, 15, 30, 0, ZoneOffset.ofHours(+1)));
    }
}
