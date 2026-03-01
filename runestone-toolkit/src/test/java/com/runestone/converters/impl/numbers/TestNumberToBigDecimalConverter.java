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

package com.runestone.converters.impl.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNumberToBigDecimalConverter {

    @Test
    public void testNumbersConversions() {
        NumberToBigDecimalConverter converter = new NumberToBigDecimalConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert((short) 1)).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(1)).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(1L)).isEqualTo(BigDecimal.valueOf(1L));
        assertThat(converter.convert(1F)).isEqualTo(BigDecimal.valueOf(1F));
        assertThat(converter.convert(1D)).isEqualTo(BigDecimal.valueOf(1D));
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo(BigDecimal.valueOf(1));
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToBigDecimalConverter converter = new NumberToBigDecimalConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new DoubleAdder())).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(converter.convert(new LongAdder())).isEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    public void testFloatingPointPrecision() {
        NumberToBigDecimalConverter converter = new NumberToBigDecimalConverter();

        // Specific problematic float values
        assertThat(converter.convert(0.1f)).isEqualTo(new BigDecimal("0.1"));
        assertThat(converter.convert(0.2f)).isEqualTo(new BigDecimal("0.2"));
        assertThat(converter.convert(0.3f)).isEqualTo(new BigDecimal("0.3"));

        // Double values
        assertThat(converter.convert(0.1d)).isEqualTo(new BigDecimal("0.1"));

        // Atomic/Accumulator versions
        DoubleAccumulator accumulator = new DoubleAccumulator(Double::sum, 0.1);
        assertThat(converter.convert(accumulator)).isEqualTo(new BigDecimal("0.1"));

        DoubleAdder adder = new DoubleAdder();
        adder.add(0.1);
        assertThat(converter.convert(adder)).isEqualTo(new BigDecimal("0.1"));
    }

}
