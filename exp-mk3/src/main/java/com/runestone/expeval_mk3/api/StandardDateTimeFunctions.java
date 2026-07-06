package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

final class StandardDateTimeFunctions {

    private StandardDateTimeFunctions() {
    }

    public static BigDecimal secondsBetween(Temporal first, Temporal second) {
        return BigDecimal.valueOf(ChronoUnit.SECONDS.between(first, second));
    }

    public static BigDecimal minutesBetween(Temporal first, Temporal second) {
        return BigDecimal.valueOf(ChronoUnit.MINUTES.between(first, second));
    }

    public static BigDecimal hoursBetween(Temporal first, Temporal second) {
        return BigDecimal.valueOf(ChronoUnit.HOURS.between(first, second));
    }

    public static BigDecimal daysBetween(Temporal first, Temporal second) {
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(first, second));
    }

    public static BigDecimal monthsBetween(Temporal first, Temporal second) {
        return BigDecimal.valueOf(ChronoUnit.MONTHS.between(first, second));
    }

    public static BigDecimal yearsBetween(Temporal first, Temporal second) {
        return BigDecimal.valueOf(ChronoUnit.YEARS.between(first, second));
    }

    public static Temporal setDay(Temporal temporal, BigDecimal day) {
        return temporal.with(ChronoField.DAY_OF_MONTH, toLong(day, "day"));
    }

    public static Temporal setMonth(Temporal temporal, BigDecimal month) {
        return temporal.with(ChronoField.MONTH_OF_YEAR, toLong(month, "month"));
    }

    public static Temporal setYear(Temporal temporal, BigDecimal year) {
        return temporal.with(ChronoField.YEAR, toLong(year, "year"));
    }

    public static Temporal setHours(Temporal temporal, BigDecimal hour) {
        return temporal.with(ChronoField.HOUR_OF_DAY, toLong(hour, "hour"));
    }

    public static Temporal setMinutes(Temporal temporal, BigDecimal minute) {
        return temporal.with(ChronoField.MINUTE_OF_HOUR, toLong(minute, "minute"));
    }

    public static Temporal setSeconds(Temporal temporal, BigDecimal second) {
        return temporal.with(ChronoField.SECOND_OF_MINUTE, toLong(second, "second"));
    }

    public static Temporal setMidnight(Temporal temporal) {
        return temporal.with(LocalTime.MIDNIGHT);
    }

    public static Temporal setMidday(Temporal temporal) {
        return temporal.with(LocalTime.NOON);
    }

    public static Temporal addDay(Temporal temporal, BigDecimal amount) {
        return temporal.plus(toLong(amount, "amount"), ChronoUnit.DAYS);
    }

    public static Temporal addMonth(Temporal temporal, BigDecimal amount) {
        return temporal.plus(toLong(amount, "amount"), ChronoUnit.MONTHS);
    }

    public static Temporal addYear(Temporal temporal, BigDecimal amount) {
        return temporal.plus(toLong(amount, "amount"), ChronoUnit.YEARS);
    }

    public static Temporal addHours(Temporal temporal, BigDecimal amount) {
        return temporal.plus(toLong(amount, "amount"), ChronoUnit.HOURS);
    }

    public static Temporal addMinutes(Temporal temporal, BigDecimal amount) {
        return temporal.plus(toLong(amount, "amount"), ChronoUnit.MINUTES);
    }

    public static Temporal addSeconds(Temporal temporal, BigDecimal amount) {
        return temporal.plus(toLong(amount, "amount"), ChronoUnit.SECONDS);
    }

    public static Temporal subDay(Temporal temporal, BigDecimal amount) {
        return temporal.minus(toLong(amount, "amount"), ChronoUnit.DAYS);
    }

    public static Temporal subMonth(Temporal temporal, BigDecimal amount) {
        return temporal.minus(toLong(amount, "amount"), ChronoUnit.MONTHS);
    }

    public static Temporal subYear(Temporal temporal, BigDecimal amount) {
        return temporal.minus(toLong(amount, "amount"), ChronoUnit.YEARS);
    }

    public static Temporal subHours(Temporal temporal, BigDecimal amount) {
        return temporal.minus(toLong(amount, "amount"), ChronoUnit.HOURS);
    }

    public static Temporal subMinutes(Temporal temporal, BigDecimal amount) {
        return temporal.minus(toLong(amount, "amount"), ChronoUnit.MINUTES);
    }

    public static Temporal subSeconds(Temporal temporal, BigDecimal amount) {
        return temporal.minus(toLong(amount, "amount"), ChronoUnit.SECONDS);
    }

    private static long toLong(BigDecimal value, String argumentName) {
        try {
            return java.util.Objects.requireNonNull(value, argumentName).longValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(argumentName + " must be an exact integer", exception);
        }
    }
}
