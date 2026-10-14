package com.runestone.converters.impl.runtime.dates;

import com.runestone.assertions.Certify;
import com.runestone.converters.ConversionContext;
import com.runestone.converters.RuntimeDataConverter;
import com.runestone.utils.DateUtils;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public final class RuntimeTemporalConverters {

    private static final List<RuntimeDataConverter<?, ?>> ALL = List.of(
            converter(java.sql.Date.class, LocalDate.class, (source, context) -> toLocalDate(source, context.zoneId())),
            converter(java.sql.Date.class, LocalDateTime.class, (source, context) -> toLocalDateTime(source, context.zoneId())),
            converter(java.sql.Date.class, LocalTime.class, (source, context) -> toLocalTime(source, context.zoneId())),
            converter(java.sql.Date.class, ZonedDateTime.class, (source, context) -> toZonedDateTime(source, context.zoneId())),
            converter(Date.class, LocalDate.class, (source, context) -> toLocalDate(source, context.zoneId())),
            converter(Date.class, LocalDateTime.class, (source, context) -> toLocalDateTime(source, context.zoneId())),
            converter(Date.class, LocalTime.class, (source, context) -> toLocalTime(source, context.zoneId())),
            converter(Date.class, ZonedDateTime.class, (source, context) -> toZonedDateTime(source, context.zoneId())),
            converter(String.class, Duration.class, (source, context) -> Duration.parse(source)),
            converter(String.class, Instant.class, (source, context) -> DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse(source, Instant::from)),
            converter(String.class, java.sql.Date.class, RuntimeTemporalConverters::toSqlDate),
            converter(String.class, Date.class, RuntimeTemporalConverters::toUtilDate),
            converter(String.class, LocalDate.class, (source, context) -> DateUtils.DATETIME_FORMATTER.parse(source, LocalDate::from)),
            converter(String.class, LocalDateTime.class, (source, context) -> DateUtils.DATETIME_FORMATTER.parse(source, LocalDateTime::from)),
            converter(String.class, LocalTime.class, (source, context) -> DateUtils.DATETIME_FORMATTER.parse(source, LocalTime::from)),
            converter(String.class, OffsetDateTime.class, (source, context) -> DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse(source, OffsetDateTime::from)),
            converter(String.class, OffsetTime.class, (source, context) -> DateUtils.DATETIME_FORMATTER.parse(source, OffsetTime::from)),
            converter(String.class, Period.class, (source, context) -> Period.parse(source)),
            converter(String.class, Temporal.class, RuntimeTemporalConverters::toTemporal),
            converter(String.class, Timestamp.class, RuntimeTemporalConverters::toTimestamp),
            converter(String.class, Year.class, (source, context) -> Year.parse(source)),
            converter(String.class, YearMonth.class, (source, context) -> DateUtils.YEAR_MONTH_FORMATTER.parse(source, YearMonth::from)),
            converter(String.class, ZoneId.class, (source, context) -> ZoneId.of(source)),
            converter(String.class, ZoneOffset.class, (source, context) -> ZoneOffset.of(source)),
            converter(String.class, ZonedDateTime.class, RuntimeTemporalConverters::toZonedDateTime),
            converter(Temporal.class, LocalDate.class, (source, context) -> temporalToLocalDate(source)),
            converter(Temporal.class, LocalDateTime.class, (source, context) -> temporalToLocalDateTime(source)),
            converter(Temporal.class, LocalTime.class, (source, context) -> temporalToLocalTime(source)),
            converter(Temporal.class, ZonedDateTime.class, (source, context) -> temporalToZonedDateTime(source, context.zoneId())));

    private RuntimeTemporalConverters() {
    }

    public static List<RuntimeDataConverter<?, ?>> all() {
        return ALL;
    }

    private static <S, T> RuntimeDataConverter<S, T> converter(
            Class<S> sourceType,
            Class<T> targetType,
            BiFunction<S, ConversionContext, T> conversion) {
        return new TemporalRuntimeConverter<>(sourceType, targetType, conversion);
    }

    private static LocalDate toLocalDate(Date value, ZoneId zoneId) {
        return toZonedDateTime(value, zoneId).toLocalDate();
    }

    private static LocalDateTime toLocalDateTime(Date value, ZoneId zoneId) {
        return toZonedDateTime(value, zoneId).toLocalDateTime();
    }

    private static LocalTime toLocalTime(Date value, ZoneId zoneId) {
        return toZonedDateTime(value, zoneId).toLocalTime();
    }

    private static ZonedDateTime toZonedDateTime(Date value, ZoneId zoneId) {
        return Instant.ofEpochMilli(Objects.requireNonNull(value).getTime())
                .atZone(Objects.requireNonNull(zoneId));
    }

    private static Temporal toTemporal(String source, ConversionContext context) {
        Certify.requireNonBlank(source, "Data must be provided");
        if (source.length() <= 8) {
            return DateUtils.DATETIME_FORMATTER.parse(source, LocalTime::from);
        } else if (source.length() <= 10) {
            return DateUtils.DATETIME_FORMATTER.parse(source, LocalDate::from);
        }
        return DateUtils.DATETIME_FORMATTER_PADDING_TIME.parse(source, LocalDateTime::from);
    }

    private static Date toUtilDate(String source, ConversionContext context) {
        Temporal temporal = convertTemporal(source);
        if (temporal instanceof LocalDate localDate) {
            return Date.from(localDate.atStartOfDay(context.zoneId()).toInstant());
        } else if (temporal instanceof LocalDateTime localDateTime) {
            return Date.from(localDateTime.atZone(context.zoneId()).toInstant());
        }
        return Date.from(((ZonedDateTime) temporal).toInstant());
    }

    private static java.sql.Date toSqlDate(String source, ConversionContext context) {
        Temporal temporal = convertTemporal(source);
        LocalDate date;
        if (temporal instanceof LocalDate localDate) {
            date = localDate;
        } else if (temporal instanceof LocalDateTime localDateTime) {
            date = localDateTime.toLocalDate();
        } else {
            date = ((ZonedDateTime) temporal).withZoneSameInstant(context.zoneId()).toLocalDate();
        }
        return new java.sql.Date(date.atStartOfDay(context.zoneId()).toInstant().toEpochMilli());
    }

    private static Timestamp toTimestamp(String source, ConversionContext context) {
        Temporal temporal = convertTemporal(source);
        if (temporal instanceof LocalDate localDate) {
            return Timestamp.from(localDate.atStartOfDay(context.zoneId()).toInstant());
        } else if (temporal instanceof LocalDateTime localDateTime) {
            return Timestamp.from(localDateTime.atZone(context.zoneId()).toInstant());
        }
        return Timestamp.from(((ZonedDateTime) temporal).toInstant());
    }

    private static ZonedDateTime toZonedDateTime(String source, ConversionContext context) {
        Temporal temporal = convertTemporal(source);
        if (temporal instanceof LocalDate localDate) {
            return localDate.atStartOfDay(context.zoneId());
        } else if (temporal instanceof LocalDateTime localDateTime) {
            return localDateTime.atZone(context.zoneId());
        }
        return (ZonedDateTime) temporal;
    }

    private static Temporal convertTemporal(String source) {
        return (Temporal) DateUtils.DATETIME_FORMATTER.parseBest(
                source,
                ZonedDateTime::from,
                LocalDateTime::from,
                LocalDate::from);
    }

    private static LocalDate temporalToLocalDate(Temporal value) {
        Temporal temporal = Objects.requireNonNull(value);
        return switch (temporal) {
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalDate();
            case LocalDateTime localDateTime -> localDateTime.toLocalDate();
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalDate();
            case LocalDate localDate -> localDate;
            default -> throw unsupported(temporal, LocalDate.class);
        };
    }

    private static LocalDateTime temporalToLocalDateTime(Temporal value) {
        Temporal temporal = Objects.requireNonNull(value);
        return switch (temporal) {
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalDateTime();
            case LocalDateTime localDateTime -> localDateTime;
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalDateTime();
            case LocalDate localDate -> localDate.atStartOfDay();
            default -> throw unsupported(temporal, LocalDateTime.class);
        };
    }

    private static LocalTime temporalToLocalTime(Temporal value) {
        Temporal temporal = Objects.requireNonNull(value);
        return switch (temporal) {
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalTime();
            case LocalDateTime localDateTime -> localDateTime.toLocalTime();
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalTime();
            case LocalTime localTime -> localTime;
            default -> throw unsupported(temporal, LocalTime.class);
        };
    }

    private static ZonedDateTime temporalToZonedDateTime(Temporal value, ZoneId zoneId) {
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

    private record TemporalRuntimeConverter<S, T>(
            Class<S> sourceType,
            Class<T> targetType,
            BiFunction<S, ConversionContext, T> conversion) implements RuntimeDataConverter<S, T> {

        private TemporalRuntimeConverter {
            Objects.requireNonNull(sourceType, "Runtime converter source type must be provided");
            Objects.requireNonNull(targetType, "Runtime converter target type must be provided");
            Objects.requireNonNull(conversion, "Runtime converter conversion must be provided");
        }

        @Override
        public T convert(S source, ConversionContext context) {
            return conversion.apply(source, context);
        }
    }
}
