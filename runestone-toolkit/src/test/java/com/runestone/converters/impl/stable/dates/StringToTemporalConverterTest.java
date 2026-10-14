package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.ConversionContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;

public class StringToTemporalConverterTest {

    private final StringToTemporalConverter converter = new StringToTemporalConverter();
    private final ConversionContext context = ConversionContext.standard();

    @Test
    public void testConvertLocalTime() {
        Temporal result = converter.convert("12:30:45", context);
        Assertions.assertThat(result).isInstanceOf(LocalTime.class);
        Assertions.assertThat(result).isEqualTo(LocalTime.of(12, 30, 45));

        Temporal shortTime = converter.convert("12:30", context);
        Assertions.assertThat(shortTime).isInstanceOf(LocalTime.class);
        Assertions.assertThat(shortTime).isEqualTo(LocalTime.of(12, 30));
    }

    @Test
    public void testConvertLocalDate() {
        Temporal result = converter.convert("2023-01-01", context);
        Assertions.assertThat(result).isInstanceOf(LocalDate.class);
        Assertions.assertThat(result).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    public void testConvertLocalDateTime() {
        Temporal result = converter.convert("2023-01-01 12:30:45", context);
        Assertions.assertThat(result).isInstanceOf(LocalDateTime.class);
        Assertions.assertThat(result).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 30, 45));

        Temporal fractionalSeconds = converter.convert("2023-01-01T12:30:45.123456789", context);
        Assertions.assertThat(fractionalSeconds).isInstanceOf(LocalDateTime.class);
        Assertions.assertThat(fractionalSeconds).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 30, 45, 123456789));

        Temporal offsetDateTime = converter.convert("2023-01-01T12:30:45Z", context);
        Assertions.assertThat(offsetDateTime).isInstanceOf(LocalDateTime.class);
        Assertions.assertThat(offsetDateTime).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 30, 45));
    }

    @Test
    public void testLeapDayBoundaries() {
        Assertions.assertThat(converter.convert("2020-02-29", context)).isEqualTo(LocalDate.of(2020, 2, 29));
        Assertions.assertThatThrownBy(() -> converter.convert("2019-02-29", context))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null, context))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testInvalidFormat() {
        // Simple string that doesn't match DATE_FORMATTER or any meaningful pattern
        Assertions.assertThatThrownBy(() -> converter.convert("NotADate", context))
                .isInstanceOf(RuntimeException.class);

        // Invalid month
        Assertions.assertThatThrownBy(() -> converter.convert("2023-13-01", context))
                .isInstanceOf(RuntimeException.class);

        // Invalid day
        Assertions.assertThatThrownBy(() -> converter.convert("2023-01-32", context))
                .isInstanceOf(RuntimeException.class);

        // Valid date but expecting time (length <= 8)
        Assertions.assertThatThrownBy(() -> converter.convert("20230101", context))
                .isInstanceOf(RuntimeException.class);
    }
}
