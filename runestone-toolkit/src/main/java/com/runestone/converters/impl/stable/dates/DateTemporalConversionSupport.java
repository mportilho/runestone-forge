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

package com.runestone.converters.impl.stable.dates;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Objects;

final class DateTemporalConversionSupport {

    private DateTemporalConversionSupport() {
    }

    static LocalDate toLocalDate(java.util.Date value, ZoneId zoneId) {
        return toZonedDateTime(value, zoneId).toLocalDate();
    }

    static LocalDateTime toLocalDateTime(java.util.Date value, ZoneId zoneId) {
        return toZonedDateTime(value, zoneId).toLocalDateTime();
    }

    static LocalTime toLocalTime(java.util.Date value, ZoneId zoneId) {
        return toZonedDateTime(value, zoneId).toLocalTime();
    }

    static ZonedDateTime toZonedDateTime(java.util.Date value, ZoneId zoneId) {
        return Instant.ofEpochMilli(Objects.requireNonNull(value).getTime())
                .atZone(Objects.requireNonNull(zoneId));
    }

    static LocalDate toLocalDate(java.sql.Date value, ZoneId zoneId) {
        return toZonedDateTime(value, zoneId).toLocalDate();
    }

    static LocalDateTime toLocalDateTime(java.sql.Date value, ZoneId zoneId) {
        return toZonedDateTime(value, zoneId).toLocalDateTime();
    }

    static LocalTime toLocalTime(java.sql.Date value, ZoneId zoneId) {
        return toZonedDateTime(value, zoneId).toLocalTime();
    }

    static ZonedDateTime toZonedDateTime(java.sql.Date value, ZoneId zoneId) {
        return Instant.ofEpochMilli(Objects.requireNonNull(value).getTime())
                .atZone(Objects.requireNonNull(zoneId));
    }

    static LocalDate temporalToLocalDate(Temporal value) {
        Temporal temporal = Objects.requireNonNull(value);
        return switch (temporal) {
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalDate();
            case LocalDateTime localDateTime -> localDateTime.toLocalDate();
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalDate();
            case LocalDate localDate -> localDate;
            default -> throw unsupported(temporal, LocalDate.class);
        };
    }

    static LocalDateTime temporalToLocalDateTime(Temporal value) {
        Temporal temporal = Objects.requireNonNull(value);
        return switch (temporal) {
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalDateTime();
            case LocalDateTime localDateTime -> localDateTime;
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalDateTime();
            case LocalDate localDate -> localDate.atStartOfDay();
            default -> throw unsupported(temporal, LocalDateTime.class);
        };
    }

    static LocalTime temporalToLocalTime(Temporal value) {
        Temporal temporal = Objects.requireNonNull(value);
        return switch (temporal) {
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalTime();
            case LocalDateTime localDateTime -> localDateTime.toLocalTime();
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalTime();
            case LocalTime localTime -> localTime;
            default -> throw unsupported(temporal, LocalTime.class);
        };
    }

    static ZonedDateTime temporalToZonedDateTime(Temporal value, ZoneId zoneId) {
        Temporal temporal = Objects.requireNonNull(value);
        ZoneId targetZone = Objects.requireNonNull(zoneId);
        return switch (temporal) {
            case ZonedDateTime zonedDateTime -> zonedDateTime;
            case LocalDateTime localDateTime -> localDateTime.atZone(targetZone);
            case LocalDate localDate -> localDate.atStartOfDay(targetZone);
            case OffsetDateTime offsetDateTime -> offsetDateTime.atZoneSameInstant(targetZone);
            default -> throw unsupported(temporal, ZonedDateTime.class);
        };
    }

    private static IllegalArgumentException unsupported(Temporal source, Class<?> targetType) {
        return new IllegalArgumentException(String.format("Unsupported conversion from [%s] to [%s].",
                source.getClass(), targetType));
    }
}
