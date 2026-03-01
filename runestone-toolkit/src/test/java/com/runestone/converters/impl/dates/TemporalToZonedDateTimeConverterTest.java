package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;

public class TemporalToZonedDateTimeConverterTest {

    private final TemporalToZonedDateTimeConverter converter = new TemporalToZonedDateTimeConverter();

    @Test
    public void testConvertFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.now();
        ZonedDateTime expected = ZonedDateTime.of(ldt, ZoneId.systemDefault());
        Assertions.assertThat(converter.convert(ldt)).isEqualTo(expected);
    }

    @Test
    public void testConvertFromLocalDate() {
        LocalDate ld = LocalDate.now();
        ZonedDateTime expected = ZonedDateTime.of(ld, LocalTime.MIDNIGHT, ZoneId.systemDefault());
        Assertions.assertThat(converter.convert(ld)).isEqualTo(expected);
    }

    @Test
    public void testConvertFromOffsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.now();
        ZonedDateTime expected = odt.atZoneSameInstant(ZoneId.systemDefault());
        Assertions.assertThat(converter.convert(odt)).isEqualTo(expected);
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
