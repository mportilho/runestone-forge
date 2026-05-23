package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TestStringToJavaSqlDateConverter {

    @Test
    public void testConvertValidValues() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01").toString())
                .isEqualTo("2021-01-01");
    }

    @Test
    public void testConvertTemporalRoutingBranches() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();

        Assertions.assertThat(converter.convert("1969-12-31"))
                .isEqualTo(Date.valueOf(LocalDate.of(1969, 12, 31)));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15"))
                .isEqualTo(Date.valueOf(LocalDate.of(2021, 1, 1)));
        Assertions.assertThat(converter.convert("2021-01-01T12:30:15.123456789Z[UTC]"))
                .isEqualTo(Date.valueOf(LocalDate.of(2021, 1, 1)));
    }

    @Test
    public void testLeapDayBoundaries() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();

        Assertions.assertThat(converter.convert("2020-02-29")).isEqualTo(Date.valueOf(LocalDate.of(2020, 2, 29)));
        Assertions.assertThatThrownBy(() -> converter.convert("2019-02-29"))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertNull() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testConvertEmptyAndBlank() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert(""))
                .isInstanceOf(DateTimeParseException.class);
        Assertions.assertThatThrownBy(() -> converter.convert("   "))
                .isInstanceOf(DateTimeParseException.class);
    }

    @Test
    public void testConvertMalformed() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThatThrownBy(() -> converter.convert("not-a-date"))
                .isInstanceOf(DateTimeParseException.class);
    }
}
