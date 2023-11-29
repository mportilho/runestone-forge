package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

public class TemporalToLocalTimeConverter implements DataConverter<Temporal, LocalTime> {

    @Override
    public LocalTime convert(Temporal data) {
        return switch (data) {
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalTime();
            case LocalDateTime localDateTime -> localDateTime.toLocalTime();
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalTime();
            case LocalTime localTime -> localTime;
            default -> throw new IllegalArgumentException(String.format("Unsupported conversion from [%s] to [%s].",
                    data.getClass(), LocalTime.class));
        };
    }
}
