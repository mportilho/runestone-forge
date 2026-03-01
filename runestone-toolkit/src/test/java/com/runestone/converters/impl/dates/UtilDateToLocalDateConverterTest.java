package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;
import java.util.Calendar;

public class UtilDateToLocalDateConverterTest {

    private final UtilDateToLocalDateConverter converter = new UtilDateToLocalDateConverter();

    @Test
    public void testConvert() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JANUARY, 1);
        Date utilDate = calendar.getTime();
        LocalDate expected = LocalDate.of(2023, 1, 1);
        Assertions.assertThat(converter.convert(utilDate)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testLeapYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2024, Calendar.FEBRUARY, 29);
        Date utilDate = calendar.getTime();
        LocalDate expected = LocalDate.of(2024, 2, 29);
        Assertions.assertThat(converter.convert(utilDate)).isEqualTo(expected);
    }
}
