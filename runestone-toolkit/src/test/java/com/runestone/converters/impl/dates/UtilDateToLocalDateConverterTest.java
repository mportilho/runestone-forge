package com.runestone.converters.impl.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class UtilDateToLocalDateConverterTest {

    private final UtilDateToLocalDateConverter converter = new UtilDateToLocalDateConverter();
    private final ConversionContext context = new ConversionContext(ZoneId.of("America/New_York"), Locale.ROOT);

    @Test
    public void testConvert() {
        Date utilDate = Date.from(Instant.parse("2023-01-01T05:00:00Z"));
        LocalDate expected = LocalDate.of(2023, 1, 1);
        Assertions.assertThat(converter.convert(utilDate, context)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testLeapYear() {
        Date utilDate = Date.from(Instant.parse("2024-02-29T12:00:00Z"));
        LocalDate expected = LocalDate.of(2024, 2, 29);
        Assertions.assertThat(converter.convert(utilDate, context)).isEqualTo(expected);
    }
}
