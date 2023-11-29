package com.runestone.expeval.support.functions.others;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

public class TestDateTimeFunctions {

    @Test
    public void testSecondsBetweenWithZonedDateTime() {
        ZonedDateTime now = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone());
        ZonedDateTime later = now.plusSeconds(10);
        Assertions.assertThat(DateTimeFunctions.secondsBetween(now, later)).isEqualTo(10);
    }

    @Test
    public void testMinutesBetweenWithLocalDateTime() {
        LocalDateTime now = LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0);
        LocalDateTime later = now.plusMinutes(10);
        Assertions.assertThat(DateTimeFunctions.minutesBetween(now, later)).isEqualTo(10);
    }

    @Test
    public void testHoursBetweenWithLocalTime() {
        LocalTime now = LocalTime.of(0, 0, 0, 0);
        LocalTime later = now.plusHours(10);
        Assertions.assertThat(DateTimeFunctions.hoursBetween(now, later)).isEqualTo(10);
    }

    @Test
    public void testDaysBetweenWithLocalDate() {
        LocalDate now = LocalDate.of(2020, 1, 1);
        LocalDate later = now.plusDays(10);
        Assertions.assertThat(DateTimeFunctions.daysBetween(now, later)).isEqualTo(10);
    }

    @Test
    public void testMonthsBetweenWithZonedDateTime() {
        ZonedDateTime now = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone());
        ZonedDateTime later = now.plusMonths(10);
        Assertions.assertThat(DateTimeFunctions.monthsBetween(now, later)).isEqualTo(10);
    }

    @Test
    public void testYearsBetweenWithLocalDateTime() {
        LocalDateTime now = LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0);
        LocalDateTime later = now.plusYears(10);
        Assertions.assertThat(DateTimeFunctions.yearsBetween(now, later)).isEqualTo(10);
    }

}
