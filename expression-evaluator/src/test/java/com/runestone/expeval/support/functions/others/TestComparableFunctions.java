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

package com.runestone.expeval.support.functions.others;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static java.math.BigDecimal.valueOf;

public class TestComparableFunctions {

    @Test
    public void testMaxNumberFunctionWithOneValue() {
        BigDecimal[] values = {valueOf(10)};
        Assertions.assertThat(ComparableFunctions.max(values)).isEqualByComparingTo("10");
    }

    @Test
    public void testMaxNumberFunctionWithTwoValues() {
        BigDecimal[] values = {valueOf(800), valueOf(9)};
        Assertions.assertThat(ComparableFunctions.max(values)).isEqualByComparingTo("800");
    }

    @Test
    public void testMaxNumberFunctionWithMultipleValues() {
        BigDecimal[] values = {valueOf(100), valueOf(250), valueOf(40), valueOf(-54), valueOf(20)};
        Assertions.assertThat(ComparableFunctions.max(values)).isEqualByComparingTo("250");
    }

    @Test
    public void testMinNumberFunctionWithOneValue() {
        BigDecimal[] values = {valueOf(1)};
        Assertions.assertThat(ComparableFunctions.min(values)).isEqualByComparingTo("1");
    }

    @Test
    public void testMinNumberFunctionWithTwoValues() {
        BigDecimal[] values = {valueOf(1), valueOf(-1)};
        Assertions.assertThat(ComparableFunctions.min(values)).isEqualByComparingTo("-1");
    }

    @Test
    public void testMinNumberFunctionWithMultipleValues() {
        BigDecimal[] values = {valueOf(-19), valueOf(-50), valueOf(1), valueOf(-100), valueOf(2)};
        Assertions.assertThat(ComparableFunctions.min(values)).isEqualByComparingTo("-100");
    }

    @Test
    public void testMaxValueWithLocalDateWithOneValue() {
        LocalDate singleValue = LocalDate.of(2020, 1, 1);
        LocalDate maxDate = ComparableFunctions.max(new LocalDate[]{singleValue});
        Assertions.assertThat(maxDate).isEqualTo(singleValue);
    }

    @Test
    public void testMaxValueWithLocalTimeWithTwoValues() {
        LocalTime firstValue = LocalTime.of(0, 0, 0, 0);
        LocalTime secondValue = LocalTime.now().plusHours(1);
        LocalTime maxTime = ComparableFunctions.max(new LocalTime[]{firstValue, secondValue});
        Assertions.assertThat(maxTime).isEqualTo(secondValue);
    }

    @Test
    public void testMaxValueWithZonedDateTimeWithMultipleValues() {
        ZonedDateTime[] values = new ZonedDateTime[5];
        for (int i = 0; i < values.length; i++) {
            values[i] = ZonedDateTime.now().plusHours(i == 3 ? 10 : i);
        }
        ZonedDateTime maxDateTime = ComparableFunctions.max(values);
        Assertions.assertThat(maxDateTime).isEqualTo(values[3]);
    }

    @Test
    public void testMinValueWithLocalTimeWithOneValue() {
        LocalTime singleValue = LocalTime.now();
        LocalTime minTime = ComparableFunctions.min(new LocalTime[]{singleValue});
        Assertions.assertThat(minTime).isEqualTo(singleValue);
    }

    @Test
    public void testMinValueWithZonedDateTimeWithTwoValues() {
        ZonedDateTime firstValue = ZonedDateTime.now();
        ZonedDateTime secondValue = ZonedDateTime.now().plusHours(1);
        ZonedDateTime minDateTime = ComparableFunctions.min(new ZonedDateTime[]{firstValue, secondValue});
        Assertions.assertThat(minDateTime).isEqualTo(firstValue);
    }

    @Test
    public void testMinValueWithLocalDateWithMultipleValues() {
        LocalDate[] values = new LocalDate[5];
        for (int i = 0; i < values.length; i++) {
            values[i] = LocalDate.now().plusDays(i == 3 ? i : i + 10);
        }
        LocalDate minDate = ComparableFunctions.min(values);
        Assertions.assertThat(minDate).isEqualTo(values[3]);
    }

}
