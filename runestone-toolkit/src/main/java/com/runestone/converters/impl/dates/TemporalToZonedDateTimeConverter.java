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

import java.time.*;
import java.time.temporal.Temporal;

public class TemporalToZonedDateTimeConverter implements DataConverter<Temporal, ZonedDateTime> {

    @Override
    public ZonedDateTime convert(Temporal data) {
        return switch (data) {
            case LocalDateTime src -> ZonedDateTime.of(src, ZoneId.systemDefault());
            case LocalDate src -> ZonedDateTime.of(src, LocalTime.MIDNIGHT, ZoneId.systemDefault());
            case OffsetDateTime src -> src.atZoneSameInstant(ZoneId.systemDefault());
            default -> throw new IllegalArgumentException(String.format("Unsupported conversion from [%s] to [%s].",
                    data.getClass(), ZonedDateTime.class));
        };
    }

}
