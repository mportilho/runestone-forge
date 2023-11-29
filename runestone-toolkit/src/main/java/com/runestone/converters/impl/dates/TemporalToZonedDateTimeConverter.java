package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.time.*;
import java.time.temporal.Temporal;

public class TemporalToZonedDateTimeConverter implements DataConverter<Temporal, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(Temporal data) {
        return switch (data) {
            case LocalDateTime src -> localDateTimeToZonedDateTime(src, null);
            case LocalDate src -> localDateToZonedDateTime(src, null);
            case OffsetDateTime src -> offsetDateTimeToZonedDateTime(src, null);
            default -> throw new IllegalArgumentException(String.format("Unsupported conversion from [%s] to [%s].",
                    data.getClass(), ZonedDateTime.class));
        };
    }

    public ZonedDateTime localDateTimeToZonedDateTime(LocalDateTime source, String format) {
        ZoneId zone = format == null || format.isBlank() ? ZoneId.systemDefault() : ZoneId.of(format);
        return ZonedDateTime.of(source, zone);
    }

    public ZonedDateTime localDateToZonedDateTime(LocalDate source, String format) {
        ZoneId zone = format == null || format.isBlank() ? ZoneId.systemDefault() : ZoneId.of(format);
        return ZonedDateTime.of(source, LocalTime.MIDNIGHT, zone);
    }

    public ZonedDateTime offsetDateTimeToZonedDateTime(OffsetDateTime source, String format) {
        ZoneId zone = format == null || format.isBlank() ? ZoneId.systemDefault() : ZoneId.of(format);
        return source.atZoneSameInstant(zone);
    }
}
