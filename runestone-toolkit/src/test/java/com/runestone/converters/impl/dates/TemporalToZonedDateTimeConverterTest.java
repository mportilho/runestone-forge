package com.runestone.converters.impl.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Locale;

public class TemporalToZonedDateTimeConverterTest {

    private final TemporalToZonedDateTimeConverter converter = new TemporalToZonedDateTimeConverter();
    private final ZoneId zoneId = ZoneId.of("America/New_York");
    private final ConversionContext context = new ConversionContext(zoneId, Locale.ROOT);

    @Test
    public void testConvertFromLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2023, 1, 1, 12, 30);
        ZonedDateTime expected = ZonedDateTime.of(ldt, zoneId);
        Assertions.assertThat(converter.convert(ldt, context)).isEqualTo(expected);
    }

    @Test
    public void testConvertFromLocalDate() {
        LocalDate ld = LocalDate.of(2023, 1, 1);
        ZonedDateTime expected = ld.atStartOfDay(zoneId);
        Assertions.assertThat(converter.convert(ld, context)).isEqualTo(expected);
    }

    @Test
    public void testConvertFromOffsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.parse("2023-01-01T12:30:00+02:00");
        ZonedDateTime expected = odt.atZoneSameInstant(zoneId);
        Assertions.assertThat(converter.convert(odt, context)).isEqualTo(expected);
    }

    @Test
    public void testConvertFromZonedDateTimeReturnsSameValue() {
        ZonedDateTime zonedDateTime = ZonedDateTime.of(
                2023, 1, 1, 12, 30, 0, 0, ZoneId.of("Europe/Paris"));

        Assertions.assertThat(converter.convert(zonedDateTime, context)).isSameAs(zonedDateTime);
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
