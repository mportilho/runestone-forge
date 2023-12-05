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

package com.runestone.utils;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DateUtils {

    public static final DateTimeFormatter YEAR_MONTH_FORMATTER;
    public static final DateTimeFormatter DATE_FORMATTER;
    public static final DateTimeFormatter TIME_FORMATTER;
    public static final DateTimeFormatter DATETIME_FORMATTER;

    public static final DateTimeFormatter DATETIME_FORMATTER_PADDING_TIME;

    static {
        YEAR_MONTH_FORMATTER = new DateTimeFormatterBuilder()
                .appendPattern("[yyyyMM][MM/yyyy][MM-yyyy][yyyy/MM][yyyy-MM]")
                .toFormatter();

        DATE_FORMATTER = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.BASIC_ISO_DATE)
                .appendPattern("[yyyy/MM/dd][yyyy-MM-dd][dd/MM/yyyy][dd-MM-yyyy]")
                .toFormatter();

        TIME_FORMATTER = new DateTimeFormatterBuilder()
                .appendPattern("HH:mm[:ss[.SSSSSSSSS][.SSSSSS][.SSS]]")
                .parseLenient()
                .optionalStart().appendZoneOrOffsetId().optionalEnd()
                .optionalStart().appendPattern("[ Z][Z][XXX]['Z']").optionalEnd()
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                .toFormatter();

        DATETIME_FORMATTER = new DateTimeFormatterBuilder()
                .append(DATE_FORMATTER)
                .optionalStart().appendPattern("['Z']['T']['z']['t'][ ][-]").optionalEnd()
                .appendOptional(TIME_FORMATTER)
                .optionalStart()
                .appendLiteral('[')
                .parseCaseSensitive()
                .appendZoneRegionId()
                .appendLiteral(']')
                .optionalEnd()
                .toFormatter();

        DATETIME_FORMATTER_PADDING_TIME = new DateTimeFormatterBuilder()
                .append(DATETIME_FORMATTER)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, 0)
                .toFormatter();
    }

    private DateUtils() {
    }
}
