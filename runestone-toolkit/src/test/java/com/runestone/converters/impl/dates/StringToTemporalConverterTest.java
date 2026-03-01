package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;

public class StringToTemporalConverterTest {

    private final StringToTemporalConverter converter = new StringToTemporalConverter();

    @Test
    public void testConvertLocalTime() {
        Temporal result = converter.convert("12:30:45");
        Assertions.assertThat(result).isInstanceOf(LocalTime.class);
        Assertions.assertThat(result).isEqualTo(LocalTime.of(12, 30, 45));
    }

    @Test
    public void testConvertLocalDate() {
        Temporal result = converter.convert("2023-01-01");
        Assertions.assertThat(result).isInstanceOf(LocalDate.class);
        Assertions.assertThat(result).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    public void testConvertLocalDateTime() {
        Temporal result = converter.convert("2023-01-01 12:30:45");
        Assertions.assertThat(result).isInstanceOf(LocalDateTime.class);
        Assertions.assertThat(result).isEqualTo(LocalDateTime.of(2023, 1, 1, 12, 30, 45));
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testInvalidFormat() {
        // Simple string that doesn't match DATE_FORMATTER or any meaningful pattern
        Assertions.assertThatThrownBy(() -> converter.convert("NotADate"))
                .isInstanceOf(RuntimeException.class);
        
        // Invalid month
        Assertions.assertThatThrownBy(() -> converter.convert("2023-13-01"))
                .isInstanceOf(RuntimeException.class);

        // Invalid day
        Assertions.assertThatThrownBy(() -> converter.convert("2023-01-32"))
                .isInstanceOf(RuntimeException.class);

        // Valid date but expecting time (length <= 8)
        Assertions.assertThatThrownBy(() -> converter.convert("20230101"))
                .isInstanceOf(RuntimeException.class);
    }
}
