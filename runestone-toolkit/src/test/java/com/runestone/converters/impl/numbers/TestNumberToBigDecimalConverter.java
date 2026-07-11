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

import com.runestone.converters.ConversionContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TestNumberToBigDecimalConverter {

    private final NumberToBigDecimalConverter converter = new NumberToBigDecimalConverter();

    @Test
    public void testNumbersConversions() {
        assertThat(converter.convert((byte) 1, ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert((short) 1, ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(1, ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(1L, ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1L));
        assertThat(converter.convert(1F, ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1F));
        assertThat(converter.convert(1D, ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1D));
        assertThat(converter.convert(BigDecimal.valueOf(1), ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new BigInteger("1"), ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1));
    }

    @Test
    public void testConcurrentNumbersConversions() {
        LongAccumulator longAccumulator = new LongAccumulator(Long::sum, 1);

        assertThat(converter.convert(new AtomicInteger(1), ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new AtomicLong(1), ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new DoubleAccumulator(Double::sum, 1), ConversionContext.standard())).isEqualByComparingTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new DoubleAdder(), ConversionContext.standard())).isEqualByComparingTo(BigDecimal.valueOf(0));
        assertThat(converter.convert(longAccumulator, ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(1));
        assertThat(converter.convert(new LongAdder(), ConversionContext.standard())).isEqualTo(BigDecimal.valueOf(0));
    }

    @Test
    public void testFloatingPointPrecision() {
        // Specific problematic float values
        assertThat(converter.convert(0.1f, ConversionContext.standard())).isEqualTo(new BigDecimal("0.1"));
        assertThat(converter.convert(0.2f, ConversionContext.standard())).isEqualTo(new BigDecimal("0.2"));
        assertThat(converter.convert(0.3f, ConversionContext.standard())).isEqualTo(new BigDecimal("0.3"));

        // Double values
        assertThat(converter.convert(0.1d, ConversionContext.standard())).isEqualTo(new BigDecimal("0.1"));

        // Atomic/Accumulator versions
        DoubleAccumulator accumulator = new DoubleAccumulator(Double::sum, 0.1);
        assertThat(converter.convert(accumulator, ConversionContext.standard())).isEqualTo(new BigDecimal("0.1"));

        DoubleAdder adder = new DoubleAdder();
        adder.add(0.1);
        assertThat(converter.convert(adder, ConversionContext.standard())).isEqualTo(new BigDecimal("0.1"));
    }

    @Test
    public void testByteConversionWithRepresentativeValues() {
        assertThat(converter.convert(Byte.MIN_VALUE, ConversionContext.standard())).isEqualByComparingTo("-128");
        assertThat(converter.convert((byte) 0, ConversionContext.standard())).isEqualByComparingTo("0");
        assertThat(converter.convert(Byte.MAX_VALUE, ConversionContext.standard())).isEqualByComparingTo("127");
    }

    @Test
    public void testShortConversionWithRepresentativeValues() {
        assertThat(converter.convert(Short.MIN_VALUE, ConversionContext.standard())).isEqualByComparingTo("-32768");
        assertThat(converter.convert((short) 0, ConversionContext.standard())).isEqualByComparingTo("0");
        assertThat(converter.convert(Short.MAX_VALUE, ConversionContext.standard())).isEqualByComparingTo("32767");
    }

    @Test
    public void testIntegerConversionWithRepresentativeValues() {
        assertThat(converter.convert(Integer.MIN_VALUE, ConversionContext.standard())).isEqualByComparingTo("-2147483648");
        assertThat(converter.convert(0, ConversionContext.standard())).isEqualByComparingTo("0");
        assertThat(converter.convert(Integer.MAX_VALUE, ConversionContext.standard())).isEqualByComparingTo("2147483647");
    }

    @Test
    public void testLongConversionWithRepresentativeValues() {
        assertThat(converter.convert(Long.MIN_VALUE, ConversionContext.standard())).isEqualByComparingTo("-9223372036854775808");
        assertThat(converter.convert(0L, ConversionContext.standard())).isEqualByComparingTo("0");
        assertThat(converter.convert(Long.MAX_VALUE, ConversionContext.standard())).isEqualByComparingTo("9223372036854775807");
    }

    @Test
    public void testFloatConversionWithRepresentativeValues() {
        assertThat(converter.convert(-12.5f, ConversionContext.standard())).isEqualByComparingTo("-12.5");
        assertThat(converter.convert(0.0f, ConversionContext.standard())).isEqualByComparingTo("0.0");
        assertThat(converter.convert(12.5f, ConversionContext.standard())).isEqualByComparingTo("12.5");
    }

    @Test
    public void testDoubleConversionWithRepresentativeValues() {
        assertThat(converter.convert(-12.5d, ConversionContext.standard())).isEqualByComparingTo("-12.5");
        assertThat(converter.convert(0.0d, ConversionContext.standard())).isEqualByComparingTo("0.0");
        assertThat(converter.convert(12.5d, ConversionContext.standard())).isEqualByComparingTo("12.5");
    }

    @Test
    public void testNullConversion() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> converter.convert((Number) null, ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot convert null to BigDecimal");
    }

    @Test
    public void testEdgeCases() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> converter.convert(Float.NaN, ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> converter.convert(Double.POSITIVE_INFINITY, ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> converter.convert(Double.NEGATIVE_INFINITY, ConversionContext.standard()))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void testUnsupportedNumberSubclass() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> converter.convert(new UnsupportedNumber(), ConversionContext.standard()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot convert " + UnsupportedNumber.class.getName() + " to BigDecimal");
    }

    private static final class UnsupportedNumber extends Number {

        @Override
        public int intValue() {
            return 1;
        }

        @Override
        public long longValue() {
            return 1L;
        }

        @Override
        public float floatValue() {
            return 1.0f;
        }

        @Override
        public double doubleValue() {
            return 1.0d;
        }
    }

}
