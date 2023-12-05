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

public class TestNumberToByteConverter {

    @Test
    public void testNumbersConversions() {
        NumberToByteConverter converter = new NumberToByteConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo((byte) 1);
        assertThat(converter.convert((short) 1)).isEqualTo((byte) 1);
        assertThat(converter.convert(1)).isEqualTo((byte) 1);
        assertThat(converter.convert(1L)).isEqualTo((byte) 1);
        assertThat(converter.convert(1F)).isEqualTo((byte) 1);
        assertThat(converter.convert(1D)).isEqualTo((byte) 1);
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo((byte) 1);
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToByteConverter converter = new NumberToByteConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new DoubleAdder())).isEqualTo((byte) 0);
        assertThat(converter.convert(new LongAccumulator(Long::sum, 1))).isEqualTo((byte) 1);
        assertThat(converter.convert(new LongAdder())).isEqualTo((byte) 0);
    }
}
