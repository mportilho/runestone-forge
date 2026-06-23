package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SqlDateToZonedDateTimeConverterTest {

    private final SqlDateToZonedDateTimeConverter converter = new SqlDateToZonedDateTimeConverter();

    @Test
    public void testConvert() {
        Date sqlDate = Date.valueOf("2023-01-01");
        ZonedDateTime expected = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());
        Assertions.assertThat(converter.convert(sqlDate)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
}
