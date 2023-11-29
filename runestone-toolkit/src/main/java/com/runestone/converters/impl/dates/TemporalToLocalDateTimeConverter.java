package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

public class TemporalToLocalDateTimeConverter implements DataConverter<Temporal, LocalDateTime> {

    @Override
    public LocalDateTime convert(Temporal data) {
        return switch (data) {
            case ZonedDateTime zonedDateTime -> zonedDateTime.toLocalDateTime();
            case LocalDateTime localDateTime -> localDateTime;
            case OffsetDateTime offsetDateTime -> offsetDateTime.toLocalDateTime();
            case LocalDate localDate -> localDate.atStartOfDay();
            default -> throw new IllegalArgumentException(String.format("Unsupported conversion from [%s] to [%s].",
                    data.getClass(), LocalDateTime.class));
        };
    }
}
