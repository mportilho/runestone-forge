package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TemporalToLocalTimeConverterTest {

    private final TemporalToLocalTimeConverter converter = new TemporalToLocalTimeConverter();
    private final ConversionContext context = ConversionContext.standard();

    @Test
    public void testConvertFromZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2023, 1, 1, 12, 30, 0, 0, ZoneId.of("Europe/Paris"));
        Assertions.assertThat(converter.convert(zdt, context)).isEqualTo(zdt.toLocalTime());
    }

    @Test
    public void testConvertFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 1, 12, 30);
        Assertions.assertThat(converter.convert(ldt, context)).isEqualTo(ldt.toLocalTime());
    }

    @Test
    public void testConvertFromOffsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.parse("2023-01-01T12:30:00+02:00");
        Assertions.assertThat(converter.convert(odt, context)).isEqualTo(odt.toLocalTime());
    }

    @Test
    public void testConvertFromLocalTime() {
        LocalTime lt = LocalTime.NOON;
        Assertions.assertThat(converter.convert(lt, context)).isEqualTo(lt);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testUnsupportedTemporal() {
        Assertions.assertThatThrownBy(() -> converter.convert(java.time.LocalDate.of(2023, 1, 1), context))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
