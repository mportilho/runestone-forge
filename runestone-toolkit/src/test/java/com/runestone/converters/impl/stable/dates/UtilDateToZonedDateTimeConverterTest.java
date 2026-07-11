package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;

public class UtilDateToZonedDateTimeConverterTest {

    private final UtilDateToZonedDateTimeConverter converter = new UtilDateToZonedDateTimeConverter();
    private final ZoneId zoneId = ZoneId.of("Asia/Tokyo");
    private final ConversionContext context = new ConversionContext(zoneId, Locale.ROOT);

    @Test
    public void testConvert() {
        Date utilDate = Date.from(Instant.parse("2023-01-01T03:00:00Z"));
        ZonedDateTime expected = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, zoneId);
        Assertions.assertThat(converter.convert(utilDate, context)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(NullPointerException.class);
    }
}
