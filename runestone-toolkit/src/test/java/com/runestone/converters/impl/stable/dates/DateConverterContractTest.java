package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.DataConverter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.List;

import static org.assertj.core.groups.Tuple.tuple;

public class DateConverterContractTest {

    @Test
    public void testConverterDefinitions() {
        List<DataConverter<?, ?>> converters = List.of(
                new SqlDateToLocalDateConverter(),
                new SqlDateToLocalDateTimeConverter(),
                new SqlDateToLocalTimeConverter(),
                new SqlDateToZonedDateTimeConverter(),
                new StringToTemporalConverter(),
                new TemporalToLocalDateConverter(),
                new TemporalToLocalDateTimeConverter(),
                new TemporalToLocalTimeConverter(),
                new TemporalToZonedDateTimeConverter(),
                new UtilDateToLocalDateConverter(),
                new UtilDateToLocalDateTimeConverter(),
                new UtilDateToLocalTimeConverter(),
                new UtilDateToZonedDateTimeConverter());

        Assertions.assertThat(converters)
                .extracting(DataConverter::sourceType, DataConverter::targetType, DataConverter::ruleIdentity)
                .containsExactly(
                        tuple(java.sql.Date.class, LocalDate.class, "dates.sql-date-to-local-date"),
                        tuple(java.sql.Date.class, LocalDateTime.class, "dates.sql-date-to-local-date-time"),
                        tuple(java.sql.Date.class, LocalTime.class, "dates.sql-date-to-local-time"),
                        tuple(java.sql.Date.class, ZonedDateTime.class, "dates.sql-date-to-zoned-date-time"),
                        tuple(String.class, Temporal.class, "dates.string-to-temporal"),
                        tuple(Temporal.class, LocalDate.class, "dates.temporal-to-local-date"),
                        tuple(Temporal.class, LocalDateTime.class, "dates.temporal-to-local-date-time"),
                        tuple(Temporal.class, LocalTime.class, "dates.temporal-to-local-time"),
                        tuple(Temporal.class, ZonedDateTime.class, "dates.temporal-to-zoned-date-time"),
                        tuple(java.util.Date.class, LocalDate.class, "dates.util-date-to-local-date"),
                        tuple(java.util.Date.class, LocalDateTime.class, "dates.util-date-to-local-date-time"),
                        tuple(java.util.Date.class, LocalTime.class, "dates.util-date-to-local-time"),
                        tuple(java.util.Date.class, ZonedDateTime.class, "dates.util-date-to-zoned-date-time"));

        Assertions.assertThat(converters)
                .extracting(DataConverter::ruleIdentity)
                .allMatch(identity -> identity.matches("[a-z0-9][a-z0-9._:-]*"))
                .doesNotHaveDuplicates();
    }
}
