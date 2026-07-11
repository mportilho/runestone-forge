package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

public class SqlDateToZonedDateTimeConverterTest {

    private final SqlDateToZonedDateTimeConverter converter = new SqlDateToZonedDateTimeConverter();
    private final ZoneId zoneId = ZoneId.of("Asia/Tokyo");
    private final ConversionContext context = new ConversionContext(zoneId, Locale.ROOT);

    @Test
    public void testConvert() {
        Date sqlDate = new Date(Instant.parse("2023-01-01T03:00:00Z").toEpochMilli());
        ZonedDateTime expected = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, zoneId);
        Assertions.assertThat(converter.convert(sqlDate, context)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(NullPointerException.class);
    }
}
