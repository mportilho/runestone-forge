package com.runestone.converters.impl.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;

public class SqlDateToLocalDateTimeConverterTest {

    private final SqlDateToLocalDateTimeConverter converter = new SqlDateToLocalDateTimeConverter();
    private final ConversionContext context = new ConversionContext(ZoneId.of("Asia/Tokyo"), Locale.ROOT);

    @Test
    public void testConvert() {
        Date sqlDate = new Date(Instant.parse("2023-01-01T03:00:00Z").toEpochMilli());
        LocalDateTime expected = LocalDateTime.of(2023, 1, 1, 12, 0);
        Assertions.assertThat(converter.convert(sqlDate, context)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(NullPointerException.class);
    }
}
