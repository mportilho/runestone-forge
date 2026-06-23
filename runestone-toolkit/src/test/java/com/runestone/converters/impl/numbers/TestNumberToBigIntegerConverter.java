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

public class TestNumberToBigIntegerConverter {

    @Test
    public void testNumbersConversions() {
        NumberToBigIntegerConverter converter = new NumberToBigIntegerConverter();
        assertThat(converter.convert((byte) 1)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert((short) 1)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(1)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(1L)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(1F)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(1D)).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(BigDecimal.valueOf(1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new BigInteger("1"))).isEqualTo(BigInteger.valueOf(1));
    }

    @Test
    public void testConcurrentNumbersConversions() {
        NumberToBigIntegerConverter converter = new NumberToBigIntegerConverter();
        assertThat(converter.convert(new AtomicInteger(1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new AtomicLong(1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new DoubleAdder())).isEqualTo(BigInteger.valueOf(0));
        assertThat(converter.convert(new LongAccumulator(Long::sum, 1))).isEqualTo(BigInteger.valueOf(1));
        assertThat(converter.convert(new LongAdder())).isEqualTo(BigInteger.valueOf(0));
    }

    @Test
    public void testNullConversion() {
        var converter = new NumberToBigIntegerConverter();
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> converter.convert((Number) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot convert null to BigInteger");
    }

    @Test
    public void testEdgeCases() {
        var converter = new NumberToBigIntegerConverter();
        assertThat(converter.convert(Double.MAX_VALUE)).isEqualTo(BigInteger.valueOf(Double.valueOf(Double.MAX_VALUE).longValue()));
        assertThat(converter.convert(Float.NaN)).isEqualTo(BigInteger.valueOf(Float.valueOf(Float.NaN).longValue()));
        assertThat(converter.convert(Double.POSITIVE_INFINITY)).isEqualTo(BigInteger.valueOf(Double.valueOf(Double.POSITIVE_INFINITY).longValue()));
    }

    @Test
    public void testFloatingPointConversions() {
        var converter = new NumberToBigIntegerConverter();
        assertThat(converter.convert(0.1f)).isEqualTo(BigInteger.ZERO);
        assertThat(converter.convert(0.9d)).isEqualTo(BigInteger.ZERO);
        assertThat(converter.convert(1.5f)).isEqualTo(BigInteger.ONE);
        assertThat(converter.convert(1.9d)).isEqualTo(BigInteger.ONE);
        assertThat(converter.convert(new BigDecimal("1.9"))).isEqualTo(BigInteger.ONE);
        assertThat(converter.convert(-1.9d)).isEqualTo(BigInteger.valueOf(-1));
    }

}
