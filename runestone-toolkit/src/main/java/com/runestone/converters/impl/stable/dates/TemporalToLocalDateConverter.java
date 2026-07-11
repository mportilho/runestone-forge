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

package com.runestone.converters.impl.stable.dates;

import com.runestone.converters.ConversionContext;
import com.runestone.converters.DataConverter;

import java.time.LocalDate;
import java.time.temporal.Temporal;

public class TemporalToLocalDateConverter implements DataConverter<Temporal, LocalDate> {

    @Override
    public Class<Temporal> sourceType() {
        return Temporal.class;
    }

    @Override
    public Class<LocalDate> targetType() {
        return LocalDate.class;
    }

    @Override
    public String ruleIdentity() {
        return "dates.temporal-to-local-date";
    }

    @Override
    public LocalDate convert(Temporal source, ConversionContext context) {
        return DateTemporalConversionSupport.temporalToLocalDate(source);
    }
}
