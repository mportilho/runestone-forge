package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

public class SqlDateToLocalDateConverterTest {

    private final SqlDateToLocalDateConverter converter = new SqlDateToLocalDateConverter();
    private final ConversionContext context = new ConversionContext(ZoneId.of("America/New_York"), Locale.ROOT);

    @Test
    public void testConvert() {
        Date sqlDate = new Date(Instant.parse("2023-01-01T05:00:00Z").toEpochMilli());
        Assertions.assertThat(converter.convert(sqlDate, context)).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testLeapYear() {
        LocalDate leapDate = LocalDate.of(2024, 2, 29);
        Date sqlDate = new Date(Instant.parse("2024-02-29T12:00:00Z").toEpochMilli());
        Assertions.assertThat(converter.convert(sqlDate, context)).isEqualTo(leapDate);
    }
}
