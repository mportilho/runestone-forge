package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Locale;

public class SqlDateToLocalTimeConverterTest {

    private final SqlDateToLocalTimeConverter converter = new SqlDateToLocalTimeConverter();
    private final ConversionContext context = new ConversionContext(ZoneId.of("Asia/Tokyo"), Locale.ROOT);

    @Test
    public void testConvert() {
        Date sqlDate = new Date(Instant.parse("2023-01-01T03:00:00Z").toEpochMilli());
        LocalTime expected = LocalTime.NOON;
        Assertions.assertThat(converter.convert(sqlDate, context)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(NullPointerException.class);
    }
}
