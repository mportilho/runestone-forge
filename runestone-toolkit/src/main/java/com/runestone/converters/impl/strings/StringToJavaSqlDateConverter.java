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

package com.runestone.converters.impl.strings;

import com.runestone.converters.ConversionContext;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;

public class StringToJavaSqlDateConverter extends SimpleStringConverter<Date> {

    public StringToJavaSqlDateConverter() {
        super(Date.class, "string-to-java-sql-date");
    }

    @Override
    public Date convert(String data, ConversionContext context) {
        Temporal temporal = StringConversionUtils.convertTemporal(data);
        if (temporal instanceof LocalDate localDate) {
            return dateAtStartOfDay(localDate, context);
        } else if (temporal instanceof LocalDateTime localDateTime) {
            return dateAtStartOfDay(localDateTime.toLocalDate(), context);
        }
        return dateAtStartOfDay(((ZonedDateTime) temporal).withZoneSameInstant(context.zoneId()).toLocalDate(), context);
    }

    private static Date dateAtStartOfDay(LocalDate date, ConversionContext context) {
        return new Date(date.atStartOfDay(context.zoneId()).toInstant().toEpochMilli());
    }
}
