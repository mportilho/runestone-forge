package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class UtilDateToLocalTimeConverterTest {

    private final UtilDateToLocalTimeConverter converter = new UtilDateToLocalTimeConverter();
    private final ConversionContext context = new ConversionContext(ZoneId.of("Asia/Tokyo"), Locale.ROOT);

    @Test
    public void testConvert() {
        Date utilDate = Date.from(Instant.parse("2023-01-01T03:00:00Z"));
        LocalTime expected = LocalTime.of(12, 0);
        Assertions.assertThat(converter.convert(utilDate, context)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(NullPointerException.class);
    }
}
