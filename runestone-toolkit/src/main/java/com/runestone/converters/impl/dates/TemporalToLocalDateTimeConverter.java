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

package com.runestone.converters.impl.dates;

import com.runestone.converters.DataConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.Objects;

public class TemporalToLocalDateTimeConverter implements DataConverter<Temporal, LocalDateTime> {

    @Override
    public LocalDateTime convert(Temporal data) {
        if (Objects.requireNonNull(data) instanceof ZonedDateTime zonedDateTime) {
            return zonedDateTime.toLocalDateTime();
        } else if (data instanceof LocalDateTime localDateTime) {
            return localDateTime;
        } else if (data instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toLocalDateTime();
        } else if (data instanceof LocalDate localDate) {
            return localDate.atStartOfDay();
        }
        throw new IllegalArgumentException(String.format("Unsupported conversion from [%s] to [%s].", data.getClass(), LocalDateTime.class));
    }
}
