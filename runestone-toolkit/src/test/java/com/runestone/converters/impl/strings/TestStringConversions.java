/*
 * MIT License
 * <p>
 * Copyright (c) 2023-2023 Marcelo Silva Portilho
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.runestone.converters.impl.strings;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;

public class TestStringConversions {

    @Test
    public void testStringToBigDecimal() {
        StringToBigDecimalConverter converter = new StringToBigDecimalConverter();
        Assertions.assertThat(converter.convert("123.45")).isEqualTo(new BigDecimal("123.45"));
    }

    @Test
    public void testStringToBigInteger() {
        StringToBigIntegerConverter converter = new StringToBigIntegerConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(new BigInteger("123"));
    }

    @Test
    public void testStringToBoolean() {
        StringToBooleanConverter converter = new StringToBooleanConverter();
        Assertions.assertThat(converter.convert("true")).isEqualTo(Boolean.TRUE);
        Assertions.assertThat(converter.convert("on")).isEqualTo(Boolean.TRUE);
        Assertions.assertThat(converter.convert("yes")).isEqualTo(Boolean.TRUE);
        Assertions.assertThat(converter.convert("1")).isEqualTo(Boolean.TRUE);
        Assertions.assertThat(converter.convert("false")).isEqualTo(Boolean.FALSE);
        Assertions.assertThat(converter.convert("0")).isEqualTo(Boolean.FALSE);
        Assertions.assertThat(converter.convert("")).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void testStringToByte() {
        StringToByteConverter converter = new StringToByteConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(Byte.valueOf("123"));
    }

    @Test
    public void testStringToDouble() {
        StringToDoubleConverter converter = new StringToDoubleConverter();
        Assertions.assertThat(converter.convert("123.45")).isEqualTo(Double.valueOf("123.45"));
    }

    @Test
    public void testStringToFloat() {
        StringToFloatConverter converter = new StringToFloatConverter();
        Assertions.assertThat(converter.convert("123.45")).isEqualTo(Float.valueOf("123.45"));
    }

    @Test
    public void testStringToInstant() {
        StringToInstantConverter converter = new StringToInstantConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z")).isEqualTo("2021-01-01T00:00:00Z");
    }

    @Test
    public void testStringToInteger() {
        StringToIntegerConverter converter = new StringToIntegerConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(Integer.valueOf("123"));
    }

    @Test
    public void testStringToJavaSqlDate() {
        StringToJavaSqlDateConverter converter = new StringToJavaSqlDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01")).isEqualTo("2021-01-01");
    }

    @Test
    public void testStringToJavaUtilDate() {
        StringToJavaUtilDateConverter converter = new StringToJavaUtilDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01")).isEqualTo("2021-01-01");
    }

    @Test
    public void testStringToLocalDate() {
        StringToLocalDateConverter converter = new StringToLocalDateConverter();
        Assertions.assertThat(converter.convert("2021-01-01")).isEqualTo("2021-01-01");
    }

    @Test
    public void testStringToLocalDateTime() {
        StringToLocalDateTimeConverter converter = new StringToLocalDateTimeConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00")).isEqualTo("2021-01-01T00:00:00");
    }

    @Test
    public void testStringToLocalTime() {
        StringToLocalTimeConverter converter = new StringToLocalTimeConverter();
        Assertions.assertThat(converter.convert("00:00:00")).isEqualTo("00:00:00");
    }

    @Test
    public void testStringToLong() {
        StringToLongConverter converter = new StringToLongConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(Long.valueOf("123"));
    }

    @Test
    public void testStringToOffsetDateTime() {
        StringToOffsetDateTimeConverter converter = new StringToOffsetDateTimeConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z")).isEqualTo("2021-01-01T00:00:00Z");
    }

    @Test
    public void testStringToOffsetTime() {
        StringToOffsetTimeConverter converter = new StringToOffsetTimeConverter();
        Assertions.assertThat(converter.convert("00:00:00Z")).isEqualTo("00:00:00Z");
    }

    @Test
    public void testStringToShort() {
        StringToShortConverter converter = new StringToShortConverter();
        Assertions.assertThat(converter.convert("123")).isEqualTo(Short.valueOf("123"));
    }

    @Test
    public void testStringToTimestamp() {
        StringToTimestampConverter converter = new StringToTimestampConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z"))
                .isEqualTo(Timestamp.from(LocalDateTime.parse("2021-01-01T00:00:00").toInstant(ZonedDateTime.now().getOffset())));
    }

    @Test
    public void testStringToYear() {
        StringToYearConverter converter = new StringToYearConverter();
        Assertions.assertThat(converter.convert("2021")).isEqualTo(Year.parse("2021"));
    }

    @Test
    public void testStringToYearMonth() {
        StringToYearMonthConverter converter = new StringToYearMonthConverter();
        Assertions.assertThat(converter.convert("2021-01")).isEqualTo(YearMonth.parse("2021-01"));
    }

    @Test
    public void testStringToZonedDateTime() {
        StringToZonedDateTimeConverter converter = new StringToZonedDateTimeConverter();
        Assertions.assertThat(converter.convert("2021-01-01T00:00:00Z")).isEqualTo("2021-01-01T00:00:00Z");
    }

}
