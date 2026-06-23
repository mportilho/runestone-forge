package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Period;
import java.time.format.DateTimeParseException;

public class TestStringToPeriodConverter {

    @Test
    public void testConvertValidValues() {
        StringToPeriodConverter converter = new StringToPeriodConverter();
        Assertions.assertThat(converter.convert("P2Y")).isEqualTo(Period.ofYears(2));
        Assertions.assertThat(converter.convert("P1Y2M3D")).isEqualTo(Period.of(1, 2, 3));
    }

    @Test
    public void testConvertNull() {
        StringToPeriodConverter converter = new StringToPeriodConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToPeriodConverter converter = new StringToPeriodConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("2 years"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
