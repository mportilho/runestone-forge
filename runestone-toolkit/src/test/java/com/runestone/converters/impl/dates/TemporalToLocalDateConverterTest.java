package com.runestone.converters.impl.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TemporalToLocalDateConverterTest {

    private final TemporalToLocalDateConverter converter = new TemporalToLocalDateConverter();
    private final ConversionContext context = ConversionContext.standard();

    @Test
    public void testConvertFromZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2023, 1, 1, 12, 30, 0, 0, ZoneId.of("Europe/Paris"));
        Assertions.assertThat(converter.convert(zdt, context)).isEqualTo(zdt.toLocalDate());
    }

    @Test
    public void testConvertFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 1, 12, 30);
        Assertions.assertThat(converter.convert(ldt, context)).isEqualTo(ldt.toLocalDate());
    }

    @Test
    public void testConvertFromOffsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.parse("2023-01-01T12:30:00+02:00");
        Assertions.assertThat(converter.convert(odt, context)).isEqualTo(odt.toLocalDate());
    }

    @Test
    public void testConvertFromLocalDate() {
        LocalDate ld = LocalDate.of(2023, 1, 1);
        Assertions.assertThat(converter.convert(ld, context)).isEqualTo(ld);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testUnsupportedTemporal() {
        Assertions.assertThatThrownBy(() -> converter.convert(LocalTime.NOON, context))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
