package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

public class SqlDateToLocalDateConverterTest {

    private final SqlDateToLocalDateConverter converter = new SqlDateToLocalDateConverter();

    @Test
    public void testConvert() {
        LocalDate now = LocalDate.now();
        Date sqlDate = Date.valueOf(now);
        Assertions.assertThat(converter.convert(sqlDate)).isEqualTo(now);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testLeapYear() {
        LocalDate leapDate = LocalDate.of(2024, 2, 29);
        Date sqlDate = Date.valueOf(leapDate);
        Assertions.assertThat(converter.convert(sqlDate)).isEqualTo(leapDate);
    }
}
