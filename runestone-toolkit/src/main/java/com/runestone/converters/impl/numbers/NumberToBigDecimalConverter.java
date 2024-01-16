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

import com.runestone.converters.DataConverter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.atomic.*;

public class NumberToBigDecimalConverter implements DataConverter<Number, BigDecimal> {

    @Override
    public BigDecimal convert(Number data) {
        if (Objects.requireNonNull(data) instanceof Byte b) {
            return BigDecimal.valueOf(b);
        } else if (data instanceof Short s) {
            return BigDecimal.valueOf(s);
        } else if (data instanceof Integer i) {
            return BigDecimal.valueOf(i);
        } else if (data instanceof Long l) {
            return BigDecimal.valueOf(l);
        } else if (data instanceof Float f) {
            return BigDecimal.valueOf(f);
        } else if (data instanceof Double d) {
            return BigDecimal.valueOf(d);
        } else if (data instanceof AtomicInteger ai) {
            return BigDecimal.valueOf(ai.get());
        } else if (data instanceof AtomicLong al) {
            return BigDecimal.valueOf(al.get());
        } else if (data instanceof DoubleAccumulator da) {
            return BigDecimal.valueOf(da.get());
        } else if (data instanceof DoubleAdder da) {
            return BigDecimal.valueOf(da.sum());
        } else if (data instanceof LongAccumulator la) {
            return BigDecimal.valueOf(la.get());
        } else if (data instanceof LongAdder la) {
            return BigDecimal.valueOf(la.sum());
        } else if (data instanceof BigInteger bi) {
            return new BigDecimal(bi);
        } else if (data instanceof BigDecimal bd) {
            return bd;
        }
        throw new IllegalArgumentException("Cannot convert " + data.getClass().getName() + " to BigDecimal");
    }
}
