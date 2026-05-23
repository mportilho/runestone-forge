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

package com.runestone.converters.impl.dates;

import java.sql.Timestamp;
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

    static LocalDate toLocalDate(java.util.Date value) {
        return new java.sql.Date(Objects.requireNonNull(value).getTime()).toLocalDate();
    }

    static LocalDateTime toLocalDateTime(java.util.Date value) {
        return toTimestamp(value).toLocalDateTime();
    }

    static LocalTime toLocalTime(java.util.Date value) {
        return toLocalDateTime(value).toLocalTime();
    }

    static ZonedDateTime toZonedDateTime(java.util.Date value) {
        return ZonedDateTime.of(toLocalDateTime(value), ZoneId.systemDefault());
    }

    static LocalDate toLocalDate(java.sql.Date value) {
        return Objects.requireNonNull(value).toLocalDate();
    }

    static LocalDateTime toLocalDateTime(java.sql.Date value) {
        return toTimestamp(value).toLocalDateTime();
    }

    static LocalTime toLocalTime(java.sql.Date value) {
        return toLocalDateTime(value).toLocalTime();
    }

    static ZonedDateTime toZonedDateTime(java.sql.Date value) {
        return ZonedDateTime.of(toLocalDateTime(value), ZoneId.systemDefault());
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

    static ZonedDateTime temporalToZonedDateTime(Temporal value) {
        Temporal temporal = Objects.requireNonNull(value);
        return switch (temporal) {
            case ZonedDateTime zonedDateTime -> zonedDateTime;
            case LocalDateTime localDateTime -> ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
            case LocalDate localDate -> ZonedDateTime.of(localDate, LocalTime.MIDNIGHT, ZoneId.systemDefault());
            case OffsetDateTime offsetDateTime -> offsetDateTime.atZoneSameInstant(ZoneId.systemDefault());
            default -> throw unsupported(temporal, ZonedDateTime.class);
        };
    }

    private static Timestamp toTimestamp(java.util.Date value) {
        return new Timestamp(Objects.requireNonNull(value).getTime());
    }

    private static IllegalArgumentException unsupported(Temporal source, Class<?> targetType) {
        return new IllegalArgumentException(String.format("Unsupported conversion from [%s] to [%s].",
                source.getClass(), targetType));
    }
}
