/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
