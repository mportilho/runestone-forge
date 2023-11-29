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
