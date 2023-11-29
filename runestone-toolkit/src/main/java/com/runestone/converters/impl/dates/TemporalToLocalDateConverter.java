package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

public class TemporalToLocalDateConverter implements DataConverter<Temporal, LocalDate> {

    @Override
    public LocalDate convert(Temporal data) {
        return switch (data) {
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalDate();
            case LocalDateTime localDateTime -> localDateTime.toLocalDate();
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalDate();
            case LocalDate localDate -> localDate;
            default -> throw new IllegalArgumentException(String.format("Unsupported conversion from [%s] to [%s].",
                    data.getClass(), LocalDate.class));
        };
    }
}
