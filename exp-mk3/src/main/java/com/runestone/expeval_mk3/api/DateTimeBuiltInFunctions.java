package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

final class DateTimeBuiltInFunctions {

    private DateTimeBuiltInFunctions() {
    }

    static List<FunctionDescriptor> descriptors() {
        ReflectedFunctionImporter.ImportPlan plan = ReflectedFunctionImporter
                .importAll(DateTimeBuiltInFunctions.class, FunctionPurity.FOLDABLE);
        renameTemporalPair(plan, "secondsBetween");
        renameTemporalPair(plan, "minutesBetween");
        renameTemporalPair(plan, "hoursBetween");
        renameDatePair(plan, "daysBetween");
        renameDatePair(plan, "monthsBetween");
        renameDatePair(plan, "yearsBetween");
        renameDatePair(plan, "setDay");
        renameDatePair(plan, "setMonth");
        renameDatePair(plan, "setYear");
        renameTemporalPair(plan, "setHours");
        renameTemporalPair(plan, "setMinutes");
        renameTemporalPair(plan, "setSeconds");
        renameTemporalPair(plan, "setMidnight");
        renameTemporalPair(plan, "setMidday");
        renameDatePair(plan, "addDay");
        renameDatePair(plan, "addMonth");
        renameDatePair(plan, "addYear");
        renameTemporalPair(plan, "addHours");
        renameTemporalPair(plan, "addMinutes");
        renameTemporalPair(plan, "addSeconds");
        renameDatePair(plan, "subDay");
        renameDatePair(plan, "subMonth");
        renameDatePair(plan, "subYear");
        renameTemporalPair(plan, "subHours");
        renameTemporalPair(plan, "subMinutes");
        renameTemporalPair(plan, "subSeconds");
        return plan.toList();
    }

    private static void renameTemporalPair(ReflectedFunctionImporter.ImportPlan plan, String languageName) {
        plan.rename(languageName + "Time", languageName)
                .rename(languageName + "DateTime", languageName);
    }

    private static void renameDatePair(ReflectedFunctionImporter.ImportPlan plan, String languageName) {
        plan.rename(languageName + "Date", languageName)
                .rename(languageName + "DateTime", languageName);
    }

    public static BigDecimal secondsBetweenTime(LocalTime first, LocalTime second) {
        return BigDecimal.valueOf(ChronoUnit.SECONDS.between(first, second));
    }

    public static BigDecimal secondsBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.SECONDS.between(first, second));
    }

    public static BigDecimal minutesBetweenTime(LocalTime first, LocalTime second) {
        return BigDecimal.valueOf(ChronoUnit.MINUTES.between(first, second));
    }

    public static BigDecimal minutesBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.MINUTES.between(first, second));
    }

    public static BigDecimal hoursBetweenTime(LocalTime first, LocalTime second) {
        return BigDecimal.valueOf(ChronoUnit.HOURS.between(first, second));
    }

    public static BigDecimal hoursBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.HOURS.between(first, second));
    }

    public static BigDecimal daysBetweenDate(LocalDate first, LocalDate second) {
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(first, second));
    }

    public static BigDecimal daysBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(first, second));
    }

    public static BigDecimal monthsBetweenDate(LocalDate first, LocalDate second) {
        return BigDecimal.valueOf(ChronoUnit.MONTHS.between(first, second));
    }

    public static BigDecimal monthsBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.MONTHS.between(first, second));
    }

    public static BigDecimal yearsBetweenDate(LocalDate first, LocalDate second) {
        return BigDecimal.valueOf(ChronoUnit.YEARS.between(first, second));
    }

    public static BigDecimal yearsBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.YEARS.between(first, second));
    }

    public static LocalDate setDayDate(LocalDate value, BigDecimal day) {
        return value.withDayOfMonth(BuiltInFunctionSupport.integer(day));
    }

    public static LocalDateTime setDayDateTime(LocalDateTime value, BigDecimal day) {
        return value.withDayOfMonth(BuiltInFunctionSupport.integer(day));
    }

    public static LocalDate setMonthDate(LocalDate value, BigDecimal month) {
        return value.withMonth(BuiltInFunctionSupport.integer(month));
    }

    public static LocalDateTime setMonthDateTime(LocalDateTime value, BigDecimal month) {
        return value.withMonth(BuiltInFunctionSupport.integer(month));
    }

    public static LocalDate setYearDate(LocalDate value, BigDecimal year) {
        return value.withYear(BuiltInFunctionSupport.integer(year));
    }

    public static LocalDateTime setYearDateTime(LocalDateTime value, BigDecimal year) {
        return value.withYear(BuiltInFunctionSupport.integer(year));
    }

    public static LocalTime setHoursTime(LocalTime value, BigDecimal hour) {
        return value.withHour(BuiltInFunctionSupport.integer(hour));
    }

    public static LocalDateTime setHoursDateTime(LocalDateTime value, BigDecimal hour) {
        return value.withHour(BuiltInFunctionSupport.integer(hour));
    }

    public static LocalTime setMinutesTime(LocalTime value, BigDecimal minute) {
        return value.withMinute(BuiltInFunctionSupport.integer(minute));
    }

    public static LocalDateTime setMinutesDateTime(LocalDateTime value, BigDecimal minute) {
        return value.withMinute(BuiltInFunctionSupport.integer(minute));
    }

    public static LocalTime setSecondsTime(LocalTime value, BigDecimal second) {
        return value.withSecond(BuiltInFunctionSupport.integer(second));
    }

    public static LocalDateTime setSecondsDateTime(LocalDateTime value, BigDecimal second) {
        return value.withSecond(BuiltInFunctionSupport.integer(second));
    }

    public static LocalTime setMidnightTime(LocalTime ignored) {
        return LocalTime.MIDNIGHT;
    }

    public static LocalDateTime setMidnightDateTime(LocalDateTime value) {
        return value.with(LocalTime.MIDNIGHT);
    }

    public static LocalTime setMiddayTime(LocalTime ignored) {
        return LocalTime.NOON;
    }

    public static LocalDateTime setMiddayDateTime(LocalDateTime value) {
        return value.with(LocalTime.NOON);
    }

    public static LocalDate addDayDate(LocalDate value, BigDecimal amount) {
        return value.plusDays(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime addDayDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusDays(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDate addMonthDate(LocalDate value, BigDecimal amount) {
        return value.plusMonths(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime addMonthDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusMonths(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDate addYearDate(LocalDate value, BigDecimal amount) {
        return value.plusYears(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime addYearDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusYears(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalTime addHoursTime(LocalTime value, BigDecimal amount) {
        return value.plusHours(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime addHoursDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusHours(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalTime addMinutesTime(LocalTime value, BigDecimal amount) {
        return value.plusMinutes(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime addMinutesDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusMinutes(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalTime addSecondsTime(LocalTime value, BigDecimal amount) {
        return value.plusSeconds(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime addSecondsDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusSeconds(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDate subDayDate(LocalDate value, BigDecimal amount) {
        return value.minusDays(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime subDayDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusDays(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDate subMonthDate(LocalDate value, BigDecimal amount) {
        return value.minusMonths(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime subMonthDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusMonths(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDate subYearDate(LocalDate value, BigDecimal amount) {
        return value.minusYears(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime subYearDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusYears(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalTime subHoursTime(LocalTime value, BigDecimal amount) {
        return value.minusHours(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime subHoursDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusHours(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalTime subMinutesTime(LocalTime value, BigDecimal amount) {
        return value.minusMinutes(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime subMinutesDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusMinutes(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalTime subSecondsTime(LocalTime value, BigDecimal amount) {
        return value.minusSeconds(BuiltInFunctionSupport.longValue(amount));
    }

    public static LocalDateTime subSecondsDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusSeconds(BuiltInFunctionSupport.longValue(amount));
    }
}
