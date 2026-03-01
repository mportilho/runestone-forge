package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public class TemporalToLocalDateConverterTest {

    private final TemporalToLocalDateConverter converter = new TemporalToLocalDateConverter();

    @Test
    public void testConvertFromZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.now();
        Assertions.assertThat(converter.convert(zdt)).isEqualTo(zdt.toLocalDate());
    }

    @Test
    public void testConvertFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.now();
        Assertions.assertThat(converter.convert(ldt)).isEqualTo(ldt.toLocalDate());
    }

    @Test
    public void testConvertFromOffsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.now();
        Assertions.assertThat(converter.convert(odt)).isEqualTo(odt.toLocalDate());
    }

    @Test
    public void testConvertFromLocalDate() {
        LocalDate ld = LocalDate.now();
        Assertions.assertThat(converter.convert(ld)).isEqualTo(ld);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testUnsupportedTemporal() {
        Assertions.assertThatThrownBy(() -> converter.convert(java.time.LocalTime.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
