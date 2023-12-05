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
import java.util.concurrent.atomic.*;

public class NumberToBigDecimalConverter implements DataConverter<Number, BigDecimal> {

    @Override
    public BigDecimal convert(Number data) {
        return switch (data) {
            case Byte b -> BigDecimal.valueOf(b);
            case Short s -> BigDecimal.valueOf(s);
            case Integer i -> BigDecimal.valueOf(i);
            case Long l -> BigDecimal.valueOf(l);
            case Float f -> BigDecimal.valueOf(f);
            case Double d -> BigDecimal.valueOf(d);
            case AtomicInteger ai -> BigDecimal.valueOf(ai.get());
            case AtomicLong al -> BigDecimal.valueOf(al.get());
            case DoubleAccumulator da -> BigDecimal.valueOf(da.get());
            case DoubleAdder da -> BigDecimal.valueOf(da.sum());
            case LongAccumulator la -> BigDecimal.valueOf(la.get());
            case LongAdder la -> BigDecimal.valueOf(la.sum());
            case BigInteger bi -> new BigDecimal(bi);
            case BigDecimal bd -> bd;
            default ->
                    throw new IllegalArgumentException("Cannot convert " + data.getClass().getName() + " to BigDecimal");
        };
    }
}
