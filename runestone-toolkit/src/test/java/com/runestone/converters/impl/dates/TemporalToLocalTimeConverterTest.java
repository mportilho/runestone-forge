package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

public class TemporalToLocalTimeConverterTest {

    private final TemporalToLocalTimeConverter converter = new TemporalToLocalTimeConverter();

    @Test
    public void testConvertFromZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.now();
        Assertions.assertThat(converter.convert(zdt)).isEqualTo(zdt.toLocalTime());
    }

    @Test
    public void testConvertFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.now();
        Assertions.assertThat(converter.convert(ldt)).isEqualTo(ldt.toLocalTime());
    }

    @Test
    public void testConvertFromOffsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.now();
        Assertions.assertThat(converter.convert(odt)).isEqualTo(odt.toLocalTime());
    }

    @Test
    public void testConvertFromLocalTime() {
        LocalTime lt = LocalTime.now();
        Assertions.assertThat(converter.convert(lt)).isEqualTo(lt);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testUnsupportedTemporal() {
        Assertions.assertThatThrownBy(() -> converter.convert(java.time.LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
