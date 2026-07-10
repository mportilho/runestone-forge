package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

final class ComparableBuiltInFunctions {

    private ComparableBuiltInFunctions() {
    }

    static List<FunctionDescriptor> descriptors() {
        return ReflectedFunctionImporter
                .importAll(ComparableBuiltInFunctions.class, FunctionPurity.FOLDABLE)
                .rename("maxNumber", "max")
                .rename("maxString", "max")
                .rename("maxDate", "max")
                .rename("maxTime", "max")
                .rename("maxDateTime", "max")
                .rename("minNumber", "min")
                .rename("minString", "min")
                .rename("minDate", "min")
                .rename("minTime", "min")
                .rename("minDateTime", "min")
                .toList();
    }

    public static BigDecimal maxNumber(List<BigDecimal> values) {
        return BuiltInFunctionSupport.max(BuiltInFunctionSupport.cast(values, BigDecimal.class));
    }

    public static BigDecimal minNumber(List<BigDecimal> values) {
        return BuiltInFunctionSupport.min(BuiltInFunctionSupport.cast(values, BigDecimal.class));
    }

    public static String maxString(List<String> values) {
        return BuiltInFunctionSupport.max(BuiltInFunctionSupport.cast(values, String.class));
    }

    public static String minString(List<String> values) {
        return BuiltInFunctionSupport.min(BuiltInFunctionSupport.cast(values, String.class));
    }

    public static LocalDate maxDate(List<LocalDate> values) {
        return BuiltInFunctionSupport.max(BuiltInFunctionSupport.cast(values, LocalDate.class));
    }

    public static LocalDate minDate(List<LocalDate> values) {
        return BuiltInFunctionSupport.min(BuiltInFunctionSupport.cast(values, LocalDate.class));
    }

    public static LocalTime maxTime(List<LocalTime> values) {
        return BuiltInFunctionSupport.max(BuiltInFunctionSupport.cast(values, LocalTime.class));
    }

    public static LocalTime minTime(List<LocalTime> values) {
        return BuiltInFunctionSupport.min(BuiltInFunctionSupport.cast(values, LocalTime.class));
    }

    public static LocalDateTime maxDateTime(List<LocalDateTime> values) {
        return BuiltInFunctionSupport.max(BuiltInFunctionSupport.cast(values, LocalDateTime.class));
    }

    public static LocalDateTime minDateTime(List<LocalDateTime> values) {
        return BuiltInFunctionSupport.min(BuiltInFunctionSupport.cast(values, LocalDateTime.class));
    }
}
