package com.runestone.converters.impl.dates;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Calendar;

public class UtilDateToLocalDateTimeConverterTest {

    private final UtilDateToLocalDateTimeConverter converter = new UtilDateToLocalDateTimeConverter();

    @Test
    public void testConvert() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2023, Calendar.JANUARY, 1, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date utilDate = calendar.getTime();
        LocalDateTime expected = LocalDateTime.of(2023, 1, 1, 12, 0);
        Assertions.assertThat(converter.convert(utilDate)).isEqualTo(expected);
    }

    @Test
    public void testNull() {
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }
}
