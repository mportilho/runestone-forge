package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDateTime;

public class SqlDateToLocalDateTimeConverterTest {

    private final SqlDateToLocalDateTimeConverter converter = new SqlDateToLocalDateTimeConverter();

    @Test
    public void testConvert() {
        Date sqlDate = Date.valueOf("2023-01-01");
        LocalDateTime expected = LocalDateTime.of(2023, 1, 1, 0, 0);
        Assertions.assertThat(converter.convert(sqlDate)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
}
