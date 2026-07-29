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

    static List<FunctionDescriptor> descriptors(BuiltInResolutionContext context) {
        ReflectedFunctionImporter.ImportPlan plan = ReflectedFunctionImporter
                .importAll(DateTimeBuiltInFunctions.class, FunctionPurity.FOLDABLE);
        plan = renameTemporalPair(plan, "secondsBetween");
        plan = renameTemporalPair(plan, "minutesBetween");
        plan = renameTemporalPair(plan, "hoursBetween");
        plan = renameDatePair(plan, "daysBetween");
        plan = renameDatePair(plan, "monthsBetween");
        plan = renameDatePair(plan, "yearsBetween");
        plan = renameDatePair(plan, "setDay");
        plan = renameDatePair(plan, "setMonth");
        plan = renameDatePair(plan, "setYear");
        plan = renameTemporalPair(plan, "setHours");
        plan = renameTemporalPair(plan, "setMinutes");
        plan = renameTemporalPair(plan, "setSeconds");
        plan = renameTemporalPair(plan, "setMidnight");
        plan = renameTemporalPair(plan, "setMidday");
        plan = renameDatePair(plan, "addDay");
        plan = renameDatePair(plan, "addMonth");
        plan = renameDatePair(plan, "addYear");
        plan = renameTemporalPair(plan, "addHours");
        plan = renameTemporalPair(plan, "addMinutes");
        plan = renameTemporalPair(plan, "addSeconds");
        plan = renameDatePair(plan, "subDay");
        plan = renameDatePair(plan, "subMonth");
        plan = renameDatePair(plan, "subYear");
        plan = renameTemporalPair(plan, "subHours");
        plan = renameTemporalPair(plan, "subMinutes");
        plan = renameTemporalPair(plan, "subSeconds");
        return ReflectedFunctionImporter.importTrustedOrThrow(plan, context);
    }

    private static ReflectedFunctionImporter.ImportPlan renameTemporalPair(
            ReflectedFunctionImporter.ImportPlan plan,
            String languageName) {
        return plan.rename(languageName + "Time", languageName)
                .rename(languageName + "DateTime", languageName);
    }

    private static ReflectedFunctionImporter.ImportPlan renameDatePair(
            ReflectedFunctionImporter.ImportPlan plan,
            String languageName) {
        return plan.rename(languageName + "Date", languageName)
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
