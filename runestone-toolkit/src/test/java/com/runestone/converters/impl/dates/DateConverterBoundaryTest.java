package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class DateConverterBoundaryTest {

    @Test
    public void testUtilDateConvertersUseControlledDefaultTimezoneForEpochAndPreEpochInstants() {
        withDefaultTimeZone("America/New_York", () -> {
            ZoneId zoneId = ZoneId.systemDefault();
            java.util.Date epoch = java.util.Date.from(Instant.EPOCH);
            java.util.Date preEpoch = java.util.Date.from(Instant.parse("1960-01-01T03:30:00Z"));

            Assertions.assertThat(new UtilDateToLocalDateConverter().convert(epoch))
                    .isEqualTo(LocalDateTime.ofInstant(Instant.EPOCH, zoneId).toLocalDate());
            Assertions.assertThat(new UtilDateToLocalDateTimeConverter().convert(preEpoch))
                    .isEqualTo(LocalDateTime.of(1959, 12, 31, 22, 30));
            Assertions.assertThat(new UtilDateToLocalTimeConverter().convert(preEpoch))
                    .isEqualTo(LocalTime.of(22, 30));
            Assertions.assertThat(new UtilDateToZonedDateTimeConverter().convert(preEpoch))
                    .isEqualTo(ZonedDateTime.of(LocalDateTime.of(1959, 12, 31, 22, 30), zoneId));
        });
    }

    @Test
    public void testUtilDateConvertersHandleDstGapInstant() {
        withDefaultTimeZone("America/New_York", () -> {
            ZoneId zoneId = ZoneId.systemDefault();
            Instant instantDuringDstGap = Instant.parse("2021-03-14T07:30:00Z");
            java.util.Date date = java.util.Date.from(instantDuringDstGap);

            Assertions.assertThat(new UtilDateToLocalDateTimeConverter().convert(date))
                    .isEqualTo(LocalDateTime.of(2021, 3, 14, 3, 30));
            Assertions.assertThat(new UtilDateToZonedDateTimeConverter().convert(date))
                    .isEqualTo(ZonedDateTime.of(LocalDateTime.of(2021, 3, 14, 3, 30), zoneId));
        });
    }

    @Test
    public void testSqlDateConvertersDocumentNonMidnightMillisBehavior() {
        withDefaultTimeZone("America/New_York", () -> {
            ZoneId zoneId = ZoneId.systemDefault();
            java.sql.Date sqlDate = new java.sql.Date(Instant.parse("2021-03-14T07:30:00Z").toEpochMilli());

            Assertions.assertThat(new SqlDateToLocalDateConverter().convert(sqlDate))
                    .isEqualTo(LocalDate.of(2021, 3, 14));
            Assertions.assertThat(new SqlDateToLocalDateTimeConverter().convert(sqlDate))
                    .isEqualTo(LocalDateTime.of(2021, 3, 14, 3, 30));
            Assertions.assertThat(new SqlDateToLocalTimeConverter().convert(sqlDate))
                    .isEqualTo(LocalTime.of(3, 30));
            Assertions.assertThat(new SqlDateToZonedDateTimeConverter().convert(sqlDate))
                    .isEqualTo(ZonedDateTime.of(LocalDateTime.of(2021, 3, 14, 3, 30), zoneId));
        });
    }

    private static void withDefaultTimeZone(String zoneId, Runnable assertions) {
        TimeZone original = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone(zoneId));
        try {
            assertions.run();
        } finally {
            TimeZone.setDefault(original);
        }
    }
}
