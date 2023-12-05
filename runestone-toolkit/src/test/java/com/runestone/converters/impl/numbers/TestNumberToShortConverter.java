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

public class TestNumberToShortConverter {

    @Test
    public void testNumbersConversions() {
        NumberToShortConverter converter = new NumberToShortConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo((short) 1);
        assertThat(converter.convert((short) 1)).isEqualTo((short) 1);
        assertThat(converter.convert(1)).isEqualTo((short) 1);
        assertThat(converter.convert(1L)).isEqualTo((short) 1);
        assertThat(converter.convert(1F)).isEqualTo((short) 1);
        assertThat(converter.convert(1D)).isEqualTo((short) 1);
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo((short) 1);
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo((short) 1);
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToShortConverter converter = new NumberToShortConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo((short) 1);
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo((short) 1);
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualTo((short) 1);
        assertThat(converter.convert(new DoubleAdder())).isEqualTo((short) 0);
        assertThat(converter.convert(new LongAccumulator(Long::sum, 1))).isEqualTo((short) 1);
        assertThat(converter.convert(new LongAdder())).isEqualTo((short) 0);
    }

}