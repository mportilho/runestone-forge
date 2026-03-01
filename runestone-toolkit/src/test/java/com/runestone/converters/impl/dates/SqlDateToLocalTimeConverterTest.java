package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalTime;

public class SqlDateToLocalTimeConverterTest {

    private final SqlDateToLocalTimeConverter converter = new SqlDateToLocalTimeConverter();

    @Test
    public void testConvert() {
        Date sqlDate = Date.valueOf("2023-01-01");
        LocalTime expected = LocalTime.MIDNIGHT;
        Assertions.assertThat(converter.convert(sqlDate)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
}
