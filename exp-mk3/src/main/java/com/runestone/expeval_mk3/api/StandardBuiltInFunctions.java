package com.runestone.expeval_mk3.api;

import ch.obermuhlner.math.big.BigDecimalMath;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

import static com.runestone.expeval_mk3.api.ScalarType.BOOLEAN;
import static com.runestone.expeval_mk3.api.ScalarType.DATE;
import static com.runestone.expeval_mk3.api.ScalarType.DATETIME;
import static com.runestone.expeval_mk3.api.ScalarType.NUMBER;
import static com.runestone.expeval_mk3.api.ScalarType.STRING;
import static com.runestone.expeval_mk3.api.ScalarType.TIME;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;

final class StandardBuiltInFunctions {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final ExpressionType NUMBER_VECTOR = new VectorType(NUMBER);
    private static final ExpressionType STRING_VECTOR = new VectorType(STRING);
    private static final ExpressionType UNKNOWN_VECTOR = new VectorType(UnknownType.INSTANCE);
    private static final int KAHAN_THRESHOLD = 1000;
    private static final double LN_2 = Math.log(2);

    private StandardBuiltInFunctions() {
    }

    static void registerAll(
            FunctionCatalog.Builder functions,
            BoundaryCoercion boundaryCoercion,
            MathContext mathContext,
            MathContext transcendentalMathContext,
            int materializationLimit) {
        Objects.requireNonNull(functions, "functions");
        Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
        Objects.requireNonNull(mathContext, "mathContext");
        Objects.requireNonNull(transcendentalMathContext, "transcendentalMathContext");
        if (materializationLimit < 0) {
            throw new IllegalArgumentException("materializationLimit must not be negative");
        }

        Map<BuiltInFunctionGroup, List<FunctionDescriptor>> groups = new EnumMap<>(BuiltInFunctionGroup.class);
        groups.put(BuiltInFunctionGroup.MATH, mathFunctions(mathContext));
        groups.put(BuiltInFunctionGroup.TRANSCENDENTAL, transcendentalFunctions(transcendentalMathContext));
        groups.put(BuiltInFunctionGroup.STRING, stringFunctions());
        groups.put(BuiltInFunctionGroup.DATE_TIME, dateTimeFunctions());
        groups.put(BuiltInFunctionGroup.COMPARABLE, comparableFunctions());
        groups.put(BuiltInFunctionGroup.FINANCIAL, financialFunctions(mathContext));
        groups.put(BuiltInFunctionGroup.ASSERTION, assertionFunctions(boundaryCoercion, materializationLimit));

        for (Map.Entry<BuiltInFunctionGroup, List<FunctionDescriptor>> entry : groups.entrySet()) {
            validateGroup(entry.getKey(), entry.getValue());
            for (FunctionDescriptor descriptor : entry.getValue()) {
                functions.register(descriptor);
            }
        }
    }

    static void validate(FunctionCatalog catalog) {
        Objects.requireNonNull(catalog, "catalog");
        for (BuiltInFunctionGroup group : BuiltInFunctionGroup.values()) {
            validateGroup(group, descriptorsForGroup(catalog, group));
        }
    }

    static void validateGroup(BuiltInFunctionGroup group, Collection<FunctionDescriptor> descriptors) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(descriptors, "descriptors");
        Set<FunctionSignature> signatures = descriptors.stream()
                .map(FunctionDescriptor::signature)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        for (FunctionSignature requiredSignature : expectedSignatures(group)) {
            if (!signatures.contains(requiredSignature)) {
                throw new IllegalStateException(group + " built-in function group is missing "
                        + requiredSignature.languageName()
                        + " signature "
                        + requiredSignature.parameterTypes());
            }
        }
    }

    static Set<FunctionSignature> expectedSignatures(BuiltInFunctionGroup group) {
        return switch (group) {
            case MATH -> signatures(
                    signature("abs", NUMBER),
                    signature("sqrt", NUMBER),
                    signature("mean", NUMBER_VECTOR),
                    signature("geometricMean", NUMBER_VECTOR),
                    signature("harmonicMean", NUMBER_VECTOR),
                    signature("variance", NUMBER_VECTOR, NUMBER),
                    signature("stdDev", NUMBER_VECTOR, NUMBER),
                    signature("meanDev", NUMBER_VECTOR),
                    signature("rule3d", NUMBER, NUMBER, NUMBER),
                    signature("rule3i", NUMBER, NUMBER, NUMBER),
                    signature("distribute", NUMBER, NUMBER, NUMBER_VECTOR, NUMBER_VECTOR),
                    signature("spread", NUMBER, NUMBER, NUMBER_VECTOR));
            case TRANSCENDENTAL -> signatures(
                    signature("sin", NUMBER),
                    signature("cos", NUMBER),
                    signature("tan", NUMBER),
                    signature("asin", NUMBER),
                    signature("acos", NUMBER),
                    signature("atan", NUMBER),
                    signature("atan2", NUMBER, NUMBER),
                    signature("sinh", NUMBER),
                    signature("cosh", NUMBER),
                    signature("tanh", NUMBER),
                    signature("asinh", NUMBER),
                    signature("acosh", NUMBER),
                    signature("atanh", NUMBER),
                    signature("ln", NUMBER),
                    signature("lb", NUMBER),
                    signature("log", NUMBER, NUMBER),
                    signature("lnFast", NUMBER),
                    signature("lbFast", NUMBER),
                    signature("logFast", NUMBER, NUMBER));
            case STRING -> signatures(
                    signature("concat", STRING_VECTOR),
                    signature("toUpper", STRING),
                    signature("toLower", STRING),
                    signature("trim", STRING),
                    signature("trimLeft", STRING),
                    signature("trimRight", STRING),
                    signature("substring", STRING, NUMBER),
                    signature("substring", STRING, NUMBER, NUMBER),
                    signature("substringBefore", STRING, STRING),
                    signature("substringAfter", STRING, STRING),
                    signature("substringBeforeLast", STRING, STRING),
                    signature("substringAfterLast", STRING, STRING),
                    signature("padLeft", STRING, NUMBER),
                    signature("padLeft", STRING, NUMBER, STRING),
                    signature("padRight", STRING, NUMBER),
                    signature("padRight", STRING, NUMBER, STRING),
                    signature("repeat", STRING, NUMBER),
                    signature("replace", STRING, STRING, STRING),
                    signature("replaceFirst", STRING, STRING, STRING),
                    signature("replaceAll", STRING, STRING, STRING),
                    signature("indexOf", STRING, STRING),
                    signature("lastIndexOf", STRING, STRING),
                    signature("startsWith", STRING, STRING),
                    signature("endsWith", STRING, STRING),
                    signature("contains", STRING, STRING),
                    signature("isEmpty", STRING),
                    signature("isBlank", STRING),
                    signature("length", STRING),
                    signature("split", STRING, STRING),
                    signature("join", UNKNOWN_VECTOR, STRING));
            case DATE_TIME -> dateTimeExpectedSignatures();
            case COMPARABLE -> signatures(
                    signature("max", new VectorType(NUMBER)),
                    signature("min", new VectorType(NUMBER)),
                    signature("max", new VectorType(STRING)),
                    signature("min", new VectorType(STRING)),
                    signature("max", new VectorType(DATE)),
                    signature("min", new VectorType(DATE)),
                    signature("max", new VectorType(TIME)),
                    signature("min", new VectorType(TIME)),
                    signature("max", new VectorType(DATETIME)),
                    signature("min", new VectorType(DATETIME)));
            case FINANCIAL -> signatures(
                    signature("fv", NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN),
                    signature("pv", NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN),
                    signature("npv", NUMBER, NUMBER_VECTOR),
                    signature("pmt", NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN),
                    signature("nper", NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN),
                    signature("pmt", NUMBER, NUMBER, NUMBER, NUMBER, NUMBER),
                    signature("pmt", NUMBER, NUMBER, NUMBER, NUMBER),
                    signature("pmt", NUMBER, NUMBER, NUMBER),
                    signature("ipmt", NUMBER, NUMBER, NUMBER, NUMBER, NUMBER, NUMBER),
                    signature("ipmt", NUMBER, NUMBER, NUMBER, NUMBER, NUMBER),
                    signature("ipmt", NUMBER, NUMBER, NUMBER, NUMBER),
                    signature("ppmt", NUMBER, NUMBER, NUMBER, NUMBER, NUMBER, NUMBER),
                    signature("ppmt", NUMBER, NUMBER, NUMBER, NUMBER, NUMBER),
                    signature("ppmt", NUMBER, NUMBER, NUMBER, NUMBER),
                    signature("fv", NUMBER, NUMBER, NUMBER, NUMBER, NUMBER),
                    signature("fv", NUMBER, NUMBER, NUMBER, NUMBER));
            case ASSERTION -> signatures(
                    signature("asNumber", UnknownType.INSTANCE),
                    signature("asText", UnknownType.INSTANCE),
                    signature("asBool", UnknownType.INSTANCE),
                    signature("asDate", UnknownType.INSTANCE),
                    signature("asTime", UnknownType.INSTANCE),
                    signature("asDateTime", UnknownType.INSTANCE),
                    signature("asVector", UnknownType.INSTANCE));
        };
    }

    private static Set<FunctionSignature> dateTimeExpectedSignatures() {
        java.util.LinkedHashSet<FunctionSignature> signatures = new java.util.LinkedHashSet<>();
        add(signatures, "secondsBetween", TIME, TIME);
        add(signatures, "secondsBetween", DATETIME, DATETIME);
        add(signatures, "minutesBetween", TIME, TIME);
        add(signatures, "minutesBetween", DATETIME, DATETIME);
        add(signatures, "hoursBetween", TIME, TIME);
        add(signatures, "hoursBetween", DATETIME, DATETIME);
        add(signatures, "daysBetween", DATE, DATE);
        add(signatures, "daysBetween", DATETIME, DATETIME);
        add(signatures, "monthsBetween", DATE, DATE);
        add(signatures, "monthsBetween", DATETIME, DATETIME);
        add(signatures, "yearsBetween", DATE, DATE);
        add(signatures, "yearsBetween", DATETIME, DATETIME);
        add(signatures, "setDay", DATE, NUMBER);
        add(signatures, "setDay", DATETIME, NUMBER);
        add(signatures, "setMonth", DATE, NUMBER);
        add(signatures, "setMonth", DATETIME, NUMBER);
        add(signatures, "setYear", DATE, NUMBER);
        add(signatures, "setYear", DATETIME, NUMBER);
        add(signatures, "setHours", TIME, NUMBER);
        add(signatures, "setHours", DATETIME, NUMBER);
        add(signatures, "setMinutes", TIME, NUMBER);
        add(signatures, "setMinutes", DATETIME, NUMBER);
        add(signatures, "setSeconds", TIME, NUMBER);
        add(signatures, "setSeconds", DATETIME, NUMBER);
        add(signatures, "setMidnight", TIME);
        add(signatures, "setMidnight", DATETIME);
        add(signatures, "setMidday", TIME);
        add(signatures, "setMidday", DATETIME);
        add(signatures, "addDay", DATE, NUMBER);
        add(signatures, "addDay", DATETIME, NUMBER);
        add(signatures, "addMonth", DATE, NUMBER);
        add(signatures, "addMonth", DATETIME, NUMBER);
        add(signatures, "addYear", DATE, NUMBER);
        add(signatures, "addYear", DATETIME, NUMBER);
        add(signatures, "addHours", TIME, NUMBER);
        add(signatures, "addHours", DATETIME, NUMBER);
        add(signatures, "addMinutes", TIME, NUMBER);
        add(signatures, "addMinutes", DATETIME, NUMBER);
        add(signatures, "addSeconds", TIME, NUMBER);
        add(signatures, "addSeconds", DATETIME, NUMBER);
        add(signatures, "subDay", DATE, NUMBER);
        add(signatures, "subDay", DATETIME, NUMBER);
        add(signatures, "subMonth", DATE, NUMBER);
        add(signatures, "subMonth", DATETIME, NUMBER);
        add(signatures, "subYear", DATE, NUMBER);
        add(signatures, "subYear", DATETIME, NUMBER);
        add(signatures, "subHours", TIME, NUMBER);
        add(signatures, "subHours", DATETIME, NUMBER);
        add(signatures, "subMinutes", TIME, NUMBER);
        add(signatures, "subMinutes", DATETIME, NUMBER);
        add(signatures, "subSeconds", TIME, NUMBER);
        add(signatures, "subSeconds", DATETIME, NUMBER);
        return Set.copyOf(signatures);
    }

    private static Set<FunctionSignature> signatures(FunctionSignature... signatures) {
        return Set.of(signatures);
    }

    private static void add(
            java.util.Set<FunctionSignature> signatures,
            String languageName,
            ExpressionType... parameterTypes) {
        signatures.add(signature(languageName, parameterTypes));
    }

    private static FunctionSignature signature(String languageName, ExpressionType... parameterTypes) {
        return new FunctionSignature(languageName, List.of(parameterTypes));
    }

    private static List<FunctionDescriptor> descriptorsForGroup(FunctionCatalog catalog, BuiltInFunctionGroup group) {
        return catalog.values().stream()
                .filter(descriptor -> group.languageNames().contains(descriptor.languageName()))
                .toList();
    }

    private static List<FunctionDescriptor> mathFunctions(MathContext mathContext) {
        return List.of(
                function("abs", List.of(NUMBER), NUMBER, "abs", classes(BigDecimal.class)),
                function("sqrt", List.of(NUMBER), NUMBER, "sqrt", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("mean", List.of(NUMBER_VECTOR), NUMBER, "mean", bound(mathContext), classes(MathContext.class, List.class)),
                function("geometricMean", List.of(NUMBER_VECTOR), NUMBER, "geometricMean", bound(mathContext), classes(MathContext.class, List.class)),
                function("harmonicMean", List.of(NUMBER_VECTOR), NUMBER, "harmonicMean", bound(mathContext), classes(MathContext.class, List.class)),
                function("variance", List.of(NUMBER_VECTOR, NUMBER), NUMBER, "variance", bound(mathContext), classes(MathContext.class, List.class, BigDecimal.class)),
                function("stdDev", List.of(NUMBER_VECTOR, NUMBER), NUMBER, "stdDev", bound(mathContext), classes(MathContext.class, List.class, BigDecimal.class)),
                function("meanDev", List.of(NUMBER_VECTOR), NUMBER, "meanDev", bound(mathContext), classes(MathContext.class, List.class)),
                function("rule3d", List.of(NUMBER, NUMBER, NUMBER), NUMBER, "rule3d", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("rule3i", List.of(NUMBER, NUMBER, NUMBER), NUMBER, "rule3i", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("distribute", List.of(NUMBER, NUMBER, NUMBER_VECTOR, NUMBER_VECTOR), NUMBER_VECTOR, "distribute", classes(BigDecimal.class, BigDecimal.class, List.class, List.class)),
                function("spread", List.of(NUMBER, NUMBER, NUMBER_VECTOR), NUMBER_VECTOR, "spread", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, List.class)));
    }

    private static List<FunctionDescriptor> transcendentalFunctions(MathContext mathContext) {
        return List.of(
                function("sin", List.of(NUMBER), NUMBER, "sin", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("cos", List.of(NUMBER), NUMBER, "cos", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("tan", List.of(NUMBER), NUMBER, "tan", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("asin", List.of(NUMBER), NUMBER, "asin", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("acos", List.of(NUMBER), NUMBER, "acos", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("atan", List.of(NUMBER), NUMBER, "atan", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("atan2", List.of(NUMBER, NUMBER), NUMBER, "atan2", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class)),
                function("sinh", List.of(NUMBER), NUMBER, "sinh", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("cosh", List.of(NUMBER), NUMBER, "cosh", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("tanh", List.of(NUMBER), NUMBER, "tanh", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("asinh", List.of(NUMBER), NUMBER, "asinh", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("acosh", List.of(NUMBER), NUMBER, "acosh", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("atanh", List.of(NUMBER), NUMBER, "atanh", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("ln", List.of(NUMBER), NUMBER, "ln", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("lb", List.of(NUMBER), NUMBER, "lb", bound(mathContext), classes(MathContext.class, BigDecimal.class)),
                function("log", List.of(NUMBER, NUMBER), NUMBER, "log", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class)),
                function("lnFast", List.of(NUMBER), NUMBER, "lnFast", classes(BigDecimal.class)),
                function("lbFast", List.of(NUMBER), NUMBER, "lbFast", classes(BigDecimal.class)),
                function("logFast", List.of(NUMBER, NUMBER), NUMBER, "logFast", classes(BigDecimal.class, BigDecimal.class)));
    }

    private static List<FunctionDescriptor> stringFunctions() {
        return List.of(
                function("concat", List.of(STRING_VECTOR), STRING, "concat", classes(List.class)),
                function("toUpper", List.of(STRING), STRING, "toUpper", classes(String.class)),
                function("toLower", List.of(STRING), STRING, "toLower", classes(String.class)),
                function("trim", List.of(STRING), STRING, "trim", classes(String.class)),
                function("trimLeft", List.of(STRING), STRING, "trimLeft", classes(String.class)),
                function("trimRight", List.of(STRING), STRING, "trimRight", classes(String.class)),
                function("substring", List.of(STRING, NUMBER), STRING, "substringFrom", classes(String.class, BigDecimal.class)),
                function("substring", List.of(STRING, NUMBER, NUMBER), STRING, "substringBetween", classes(String.class, BigDecimal.class, BigDecimal.class)),
                function("substringBefore", List.of(STRING, STRING), STRING, "substringBefore", classes(String.class, String.class)),
                function("substringAfter", List.of(STRING, STRING), STRING, "substringAfter", classes(String.class, String.class)),
                function("substringBeforeLast", List.of(STRING, STRING), STRING, "substringBeforeLast", classes(String.class, String.class)),
                function("substringAfterLast", List.of(STRING, STRING), STRING, "substringAfterLast", classes(String.class, String.class)),
                function("padLeft", List.of(STRING, NUMBER), STRING, "padLeft", classes(String.class, BigDecimal.class)),
                function("padLeft", List.of(STRING, NUMBER, STRING), STRING, "padLeftWith", classes(String.class, BigDecimal.class, String.class)),
                function("padRight", List.of(STRING, NUMBER), STRING, "padRight", classes(String.class, BigDecimal.class)),
                function("padRight", List.of(STRING, NUMBER, STRING), STRING, "padRightWith", classes(String.class, BigDecimal.class, String.class)),
                function("repeat", List.of(STRING, NUMBER), STRING, "repeat", classes(String.class, BigDecimal.class)),
                function("replace", List.of(STRING, STRING, STRING), STRING, "replace", classes(String.class, String.class, String.class)),
                function("replaceFirst", List.of(STRING, STRING, STRING), STRING, "replaceFirst", classes(String.class, String.class, String.class)),
                function("replaceAll", List.of(STRING, STRING, STRING), STRING, "replaceAll", classes(String.class, String.class, String.class)),
                function("indexOf", List.of(STRING, STRING), NUMBER, "indexOf", classes(String.class, String.class)),
                function("lastIndexOf", List.of(STRING, STRING), NUMBER, "lastIndexOf", classes(String.class, String.class)),
                function("startsWith", List.of(STRING, STRING), BOOLEAN, "startsWith", classes(String.class, String.class)),
                function("endsWith", List.of(STRING, STRING), BOOLEAN, "endsWith", classes(String.class, String.class)),
                function("contains", List.of(STRING, STRING), BOOLEAN, "contains", classes(String.class, String.class)),
                function("isEmpty", List.of(STRING), BOOLEAN, "isEmpty", classes(String.class)),
                function("isBlank", List.of(STRING), BOOLEAN, "isBlank", classes(String.class)),
                function("length", List.of(STRING), NUMBER, "length", classes(String.class)),
                function("split", List.of(STRING, STRING), STRING_VECTOR, "split", classes(String.class, String.class)),
                function("join", List.of(UNKNOWN_VECTOR, STRING), STRING, "join", classes(List.class, String.class)));
    }

    private static List<FunctionDescriptor> dateTimeFunctions() {
        return List.of(
                function("secondsBetween", List.of(TIME, TIME), NUMBER, "secondsBetweenTime", classes(LocalTime.class, LocalTime.class)),
                function("secondsBetween", List.of(DATETIME, DATETIME), NUMBER, "secondsBetweenDateTime", classes(LocalDateTime.class, LocalDateTime.class)),
                function("minutesBetween", List.of(TIME, TIME), NUMBER, "minutesBetweenTime", classes(LocalTime.class, LocalTime.class)),
                function("minutesBetween", List.of(DATETIME, DATETIME), NUMBER, "minutesBetweenDateTime", classes(LocalDateTime.class, LocalDateTime.class)),
                function("hoursBetween", List.of(TIME, TIME), NUMBER, "hoursBetweenTime", classes(LocalTime.class, LocalTime.class)),
                function("hoursBetween", List.of(DATETIME, DATETIME), NUMBER, "hoursBetweenDateTime", classes(LocalDateTime.class, LocalDateTime.class)),
                function("daysBetween", List.of(DATE, DATE), NUMBER, "daysBetweenDate", classes(LocalDate.class, LocalDate.class)),
                function("daysBetween", List.of(DATETIME, DATETIME), NUMBER, "daysBetweenDateTime", classes(LocalDateTime.class, LocalDateTime.class)),
                function("monthsBetween", List.of(DATE, DATE), NUMBER, "monthsBetweenDate", classes(LocalDate.class, LocalDate.class)),
                function("monthsBetween", List.of(DATETIME, DATETIME), NUMBER, "monthsBetweenDateTime", classes(LocalDateTime.class, LocalDateTime.class)),
                function("yearsBetween", List.of(DATE, DATE), NUMBER, "yearsBetweenDate", classes(LocalDate.class, LocalDate.class)),
                function("yearsBetween", List.of(DATETIME, DATETIME), NUMBER, "yearsBetweenDateTime", classes(LocalDateTime.class, LocalDateTime.class)),
                function("setDay", List.of(DATE, NUMBER), DATE, "setDayDate", classes(LocalDate.class, BigDecimal.class)),
                function("setDay", List.of(DATETIME, NUMBER), DATETIME, "setDayDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("setMonth", List.of(DATE, NUMBER), DATE, "setMonthDate", classes(LocalDate.class, BigDecimal.class)),
                function("setMonth", List.of(DATETIME, NUMBER), DATETIME, "setMonthDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("setYear", List.of(DATE, NUMBER), DATE, "setYearDate", classes(LocalDate.class, BigDecimal.class)),
                function("setYear", List.of(DATETIME, NUMBER), DATETIME, "setYearDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("setHours", List.of(TIME, NUMBER), TIME, "setHoursTime", classes(LocalTime.class, BigDecimal.class)),
                function("setHours", List.of(DATETIME, NUMBER), DATETIME, "setHoursDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("setMinutes", List.of(TIME, NUMBER), TIME, "setMinutesTime", classes(LocalTime.class, BigDecimal.class)),
                function("setMinutes", List.of(DATETIME, NUMBER), DATETIME, "setMinutesDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("setSeconds", List.of(TIME, NUMBER), TIME, "setSecondsTime", classes(LocalTime.class, BigDecimal.class)),
                function("setSeconds", List.of(DATETIME, NUMBER), DATETIME, "setSecondsDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("setMidnight", List.of(TIME), TIME, "setMidnightTime", classes(LocalTime.class)),
                function("setMidnight", List.of(DATETIME), DATETIME, "setMidnightDateTime", classes(LocalDateTime.class)),
                function("setMidday", List.of(TIME), TIME, "setMiddayTime", classes(LocalTime.class)),
                function("setMidday", List.of(DATETIME), DATETIME, "setMiddayDateTime", classes(LocalDateTime.class)),
                function("addDay", List.of(DATE, NUMBER), DATE, "addDayDate", classes(LocalDate.class, BigDecimal.class)),
                function("addDay", List.of(DATETIME, NUMBER), DATETIME, "addDayDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("addMonth", List.of(DATE, NUMBER), DATE, "addMonthDate", classes(LocalDate.class, BigDecimal.class)),
                function("addMonth", List.of(DATETIME, NUMBER), DATETIME, "addMonthDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("addYear", List.of(DATE, NUMBER), DATE, "addYearDate", classes(LocalDate.class, BigDecimal.class)),
                function("addYear", List.of(DATETIME, NUMBER), DATETIME, "addYearDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("addHours", List.of(TIME, NUMBER), TIME, "addHoursTime", classes(LocalTime.class, BigDecimal.class)),
                function("addHours", List.of(DATETIME, NUMBER), DATETIME, "addHoursDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("addMinutes", List.of(TIME, NUMBER), TIME, "addMinutesTime", classes(LocalTime.class, BigDecimal.class)),
                function("addMinutes", List.of(DATETIME, NUMBER), DATETIME, "addMinutesDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("addSeconds", List.of(TIME, NUMBER), TIME, "addSecondsTime", classes(LocalTime.class, BigDecimal.class)),
                function("addSeconds", List.of(DATETIME, NUMBER), DATETIME, "addSecondsDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("subDay", List.of(DATE, NUMBER), DATE, "subDayDate", classes(LocalDate.class, BigDecimal.class)),
                function("subDay", List.of(DATETIME, NUMBER), DATETIME, "subDayDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("subMonth", List.of(DATE, NUMBER), DATE, "subMonthDate", classes(LocalDate.class, BigDecimal.class)),
                function("subMonth", List.of(DATETIME, NUMBER), DATETIME, "subMonthDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("subYear", List.of(DATE, NUMBER), DATE, "subYearDate", classes(LocalDate.class, BigDecimal.class)),
                function("subYear", List.of(DATETIME, NUMBER), DATETIME, "subYearDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("subHours", List.of(TIME, NUMBER), TIME, "subHoursTime", classes(LocalTime.class, BigDecimal.class)),
                function("subHours", List.of(DATETIME, NUMBER), DATETIME, "subHoursDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("subMinutes", List.of(TIME, NUMBER), TIME, "subMinutesTime", classes(LocalTime.class, BigDecimal.class)),
                function("subMinutes", List.of(DATETIME, NUMBER), DATETIME, "subMinutesDateTime", classes(LocalDateTime.class, BigDecimal.class)),
                function("subSeconds", List.of(TIME, NUMBER), TIME, "subSecondsTime", classes(LocalTime.class, BigDecimal.class)),
                function("subSeconds", List.of(DATETIME, NUMBER), DATETIME, "subSecondsDateTime", classes(LocalDateTime.class, BigDecimal.class)));
    }

    private static List<FunctionDescriptor> comparableFunctions() {
        return List.of(
                function("max", List.of(new VectorType(NUMBER)), NUMBER, "maxNumber", classes(List.class)),
                function("min", List.of(new VectorType(NUMBER)), NUMBER, "minNumber", classes(List.class)),
                function("max", List.of(new VectorType(STRING)), STRING, "maxString", classes(List.class)),
                function("min", List.of(new VectorType(STRING)), STRING, "minString", classes(List.class)),
                function("max", List.of(new VectorType(DATE)), DATE, "maxDate", classes(List.class)),
                function("min", List.of(new VectorType(DATE)), DATE, "minDate", classes(List.class)),
                function("max", List.of(new VectorType(TIME)), TIME, "maxTime", classes(List.class)),
                function("min", List.of(new VectorType(TIME)), TIME, "minTime", classes(List.class)),
                function("max", List.of(new VectorType(DATETIME)), DATETIME, "maxDateTime", classes(List.class)),
                function("min", List.of(new VectorType(DATETIME)), DATETIME, "minDateTime", classes(List.class)));
    }

    private static List<FunctionDescriptor> financialFunctions(MathContext mathContext) {
        return List.of(
                function("fv", List.of(NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN), NUMBER, "fvRegular", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, Boolean.class)),
                function("pv", List.of(NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN), NUMBER, "pv", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, Boolean.class)),
                function("npv", List.of(NUMBER, NUMBER_VECTOR), NUMBER, "npv", bound(mathContext), classes(MathContext.class, BigDecimal.class, List.class)),
                function("pmt", List.of(NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN), NUMBER, "pmtRegular", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, Boolean.class)),
                function("nper", List.of(NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN), NUMBER, "nper", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, Boolean.class)),
                function("pmt", List.of(NUMBER, NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "pmtTyped", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("pmt", List.of(NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "pmtNoType", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("pmt", List.of(NUMBER, NUMBER, NUMBER), NUMBER, "pmtPresent", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("ipmt", List.of(NUMBER, NUMBER, NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "ipmtTyped", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("ipmt", List.of(NUMBER, NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "ipmtNoType", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("ipmt", List.of(NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "ipmtPresent", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("ppmt", List.of(NUMBER, NUMBER, NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "ppmtTyped", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("ppmt", List.of(NUMBER, NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "ppmtNoType", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("ppmt", List.of(NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "ppmtPresent", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("fv", List.of(NUMBER, NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "fvTyped", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)),
                function("fv", List.of(NUMBER, NUMBER, NUMBER, NUMBER), NUMBER, "fvNoType", bound(mathContext), classes(MathContext.class, BigDecimal.class, BigDecimal.class, BigDecimal.class, BigDecimal.class)));
    }

    private static List<FunctionDescriptor> assertionFunctions(BoundaryCoercion boundaryCoercion, int materializationLimit) {
        FunctionPurity purity = boundaryCoercion.deterministicForConstants()
                ? FunctionPurity.FOLDABLE
                : FunctionPurity.PURE;
        return List.of(
                function("asNumber", List.of(UnknownType.INSTANCE), NUMBER, "asNumber", purity, bound(boundaryCoercion), classes(BoundaryCoercion.class, Object.class)),
                function("asText", List.of(UnknownType.INSTANCE), STRING, "asText", purity, bound(boundaryCoercion), classes(BoundaryCoercion.class, Object.class)),
                function("asBool", List.of(UnknownType.INSTANCE), BOOLEAN, "asBool", purity, bound(boundaryCoercion), classes(BoundaryCoercion.class, Object.class)),
                function("asDate", List.of(UnknownType.INSTANCE), DATE, "asDate", purity, bound(boundaryCoercion), classes(BoundaryCoercion.class, Object.class)),
                function("asTime", List.of(UnknownType.INSTANCE), TIME, "asTime", purity, bound(boundaryCoercion), classes(BoundaryCoercion.class, Object.class)),
                function("asDateTime", List.of(UnknownType.INSTANCE), DATETIME, "asDateTime", purity, bound(boundaryCoercion), classes(BoundaryCoercion.class, Object.class)),
                function("asVector", List.of(UnknownType.INSTANCE), UNKNOWN_VECTOR, "asVector", FunctionPurity.FOLDABLE, bound(materializationLimit), classes(int.class, Object.class)));
    }

    private static FunctionDescriptor function(
            String languageName,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            String methodName,
            Class<?>... methodParameterTypes) {
        return function(languageName, parameterTypes, returnType, methodName, new Object[0], methodParameterTypes);
    }

    private static FunctionDescriptor function(
            String languageName,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            String methodName,
            Object[] boundArguments,
            Class<?>... methodParameterTypes) {
        return function(
                languageName,
                parameterTypes,
                returnType,
                methodName,
                FunctionPurity.FOLDABLE,
                boundArguments,
                methodParameterTypes);
    }

    private static FunctionDescriptor function(
            String languageName,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            String methodName,
            FunctionPurity purity,
            Object[] boundArguments,
            Class<?>... methodParameterTypes) {
        try {
            Method method = StandardBuiltInFunctions.class.getDeclaredMethod(methodName, methodParameterTypes);
            MethodHandle handle = LOOKUP.unreflect(method);
            if (boundArguments.length > 0) {
                handle = MethodHandles.insertArguments(handle, 0, boundArguments);
            }
            return FunctionDescriptor.fromHandle(
                    languageName,
                    handle,
                    FunctionImplementationMetadata.forMethod(method),
                    parameterTypes,
                    returnType,
                    purity);
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("failed to create built-in function descriptor for " + languageName, exception);
        }
    }

    private static Object[] bound(Object... arguments) {
        return arguments;
    }

    private static Class<?>[] classes(Class<?>... classes) {
        return classes;
    }

    static BigDecimal abs(BigDecimal value) {
        return value.abs();
    }

    static BigDecimal sqrt(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.sqrt(value, mathContext);
    }

    static BigDecimal mean(MathContext mathContext, List<?> values) {
        BigDecimal[] numbers = numbers(values);
        int size = numbers.length;
        if (size == 0) {
            throw new ArithmeticException("mean of empty vector is undefined");
        }
        if (size == 1) {
            return numbers[0];
        }
        if (size == 2) {
            return numbers[0].add(numbers[1]).divide(BigDecimal.valueOf(size), mathContext);
        }
        if (size >= KAHAN_THRESHOLD) {
            return BigDecimal.valueOf(kahanSum(numbers)).divide(BigDecimal.valueOf(size), mathContext);
        }
        BigDecimal sum = ZERO;
        for (BigDecimal number : numbers) {
            sum = sum.add(number);
        }
        return sum.divide(BigDecimal.valueOf(size), mathContext);
    }

    static BigDecimal geometricMean(MathContext mathContext, List<?> values) {
        BigDecimal[] numbers = numbers(values);
        if (numbers.length == 0) {
            throw new ArithmeticException("geometric mean of empty vector is undefined");
        }
        BigDecimal product = ONE;
        for (BigDecimal number : numbers) {
            product = product.multiply(number, mathContext);
        }
        return BigDecimalMath.root(product, BigDecimal.valueOf(numbers.length), mathContext);
    }

    static BigDecimal harmonicMean(MathContext mathContext, List<?> values) {
        BigDecimal[] numbers = numbers(values);
        BigDecimal reciprocalSum = ZERO;
        for (BigDecimal number : numbers) {
            reciprocalSum = reciprocalSum.add(ONE.divide(number, mathContext), mathContext);
        }
        return BigDecimal.valueOf(numbers.length).divide(reciprocalSum, mathContext);
    }

    static BigDecimal variance(MathContext mathContext, List<?> values, BigDecimal type) {
        BigDecimal[] numbers = numbers(values);
        int size = numbers.length;
        if (size == 0) {
            throw new ArithmeticException("variance of empty vector is undefined");
        }
        BigDecimal divisor = BigDecimal.valueOf(size - integer(type));
        if (size >= KAHAN_THRESHOLD) {
            double sum = 0.0;
            double sumSquares = 0.0;
            double sumCompensation = 0.0;
            double squareCompensation = 0.0;
            for (BigDecimal number : numbers) {
                double value = number.doubleValue();
                double correctedSum = value - sumCompensation;
                double nextSum = sum + correctedSum;
                sumCompensation = (nextSum - sum) - correctedSum;
                sum = nextSum;

                double correctedSquare = value * value - squareCompensation;
                double nextSquare = sumSquares + correctedSquare;
                squareCompensation = (nextSquare - sumSquares) - correctedSquare;
                sumSquares = nextSquare;
            }
            BigDecimal decimalSum = BigDecimal.valueOf(sum);
            BigDecimal mean = decimalSum.divide(BigDecimal.valueOf(size), mathContext);
            BigDecimal numerator = BigDecimal.valueOf(sumSquares).subtract(decimalSum.multiply(mean, mathContext), mathContext);
            return numerator.divide(divisor, mathContext);
        }
        BigDecimal sum = ZERO;
        BigDecimal sumSquares = ZERO;
        for (BigDecimal number : numbers) {
            sum = sum.add(number);
            sumSquares = sumSquares.add(number.multiply(number, mathContext), mathContext);
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(size), mathContext);
        BigDecimal numerator = sumSquares.subtract(sum.multiply(mean, mathContext), mathContext);
        return numerator.divide(divisor, mathContext);
    }

    static BigDecimal stdDev(MathContext mathContext, List<?> values, BigDecimal type) {
        return BigDecimalMath.sqrt(variance(mathContext, values, type), mathContext);
    }

    static BigDecimal meanDev(MathContext mathContext, List<?> values) {
        BigDecimal[] numbers = numbers(values);
        int size = numbers.length;
        if (size == 0) {
            throw new ArithmeticException("mean deviation of empty vector is undefined");
        }
        if (size >= KAHAN_THRESHOLD) {
            double mean = kahanSum(numbers) / size;
            return BigDecimal.valueOf(kahanSumAbsoluteDeviations(numbers, mean)).divide(BigDecimal.valueOf(size), mathContext);
        }
        BigDecimal sum = ZERO;
        for (BigDecimal number : numbers) {
            sum = sum.add(number);
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(size), mathContext);
        BigDecimal deviation = ZERO;
        for (BigDecimal number : numbers) {
            deviation = deviation.add(number.subtract(mean, mathContext).abs(mathContext), mathContext);
        }
        return deviation.divide(BigDecimal.valueOf(size), mathContext);
    }

    static BigDecimal rule3d(MathContext mathContext, BigDecimal origin1, BigDecimal result1, BigDecimal origin2) {
        return origin2.multiply(result1, mathContext).divide(origin1, mathContext);
    }

    static BigDecimal rule3i(MathContext mathContext, BigDecimal origin1, BigDecimal result1, BigDecimal origin2) {
        return origin1.multiply(result1, mathContext).divide(origin2, mathContext);
    }

    static List<BigDecimal> distribute(BigDecimal value, BigDecimal direction, List<?> targetValues, List<?> limitValues) {
        BigDecimal[] targets = numbers(targetValues);
        BigDecimal[] limits = numbers(limitValues);
        if (targets.length != limits.length) {
            throw new IllegalArgumentException("target and limits vectors must have the same size");
        }
        BigDecimal[] distributed = new BigDecimal[targets.length + 1];
        System.arraycopy(targets, 0, distributed, 0, targets.length);
        BigDecimal currentValue = value;
        if (currentValue.compareTo(ZERO) >= 0) {
            if (direction.compareTo(ZERO) >= 0) {
                for (int index = 0; index < targets.length; index++) {
                    currentValue = positiveDistribution(currentValue, targets[index], limits[index], index, distributed);
                    if (currentValue.compareTo(ZERO) == 0) {
                        break;
                    }
                }
            } else {
                for (int index = targets.length - 1; index >= 0; index--) {
                    currentValue = positiveDistribution(currentValue, targets[index], limits[index], index, distributed);
                    if (currentValue.compareTo(ZERO) == 0) {
                        break;
                    }
                }
            }
        } else {
            currentValue = currentValue.abs();
            if (direction.compareTo(ZERO) >= 0) {
                for (int index = 0; index < targets.length; index++) {
                    currentValue = negativeDistribution(currentValue, targets[index], limits[index], index, distributed);
                    if (currentValue.compareTo(ZERO) == 0) {
                        break;
                    }
                }
            } else {
                for (int index = targets.length - 1; index >= 0; index--) {
                    currentValue = negativeDistribution(currentValue, targets[index], limits[index], index, distributed);
                    if (currentValue.compareTo(ZERO) == 0) {
                        break;
                    }
                }
            }
            currentValue = currentValue.negate();
        }
        distributed[distributed.length - 1] = currentValue;
        return List.of(distributed);
    }

    static List<BigDecimal> spread(MathContext mathContext, BigDecimal value, BigDecimal direction, List<?> referenceValues) {
        BigDecimal[] references = numbers(referenceValues);
        if (references.length == 0) {
            throw new IllegalArgumentException("references vector must not be empty");
        }
        int scale = value.scale();
        if (value.compareTo(ZERO) == 0) {
            return Collections.nCopies(references.length, ZERO.setScale(scale, HALF_EVEN));
        }
        BigDecimal total = ZERO;
        for (BigDecimal reference : references) {
            total = total.add(reference);
        }
        BigDecimal[] distributed = new BigDecimal[references.length];
        BigDecimal distributedSum = ZERO;
        if (total.compareTo(ZERO) != 0) {
            BigDecimal factor = value.divide(total, mathContext);
            for (int index = 0; index < references.length; index++) {
                BigDecimal distributedValue = references[index].multiply(factor).setScale(scale, HALF_EVEN);
                distributed[index] = distributedValue;
                distributedSum = distributedSum.add(distributedValue);
            }
        } else {
            BigDecimal size = BigDecimal.valueOf(references.length);
            BigDecimal distributedValue = value.divide(size, HALF_EVEN).setScale(scale, HALF_EVEN);
            distributedSum = distributedValue.multiply(size);
            java.util.Arrays.fill(distributed, distributedValue);
        }
        BigDecimal difference = value.subtract(distributedSum).setScale(scale, HALF_EVEN);
        if (difference.compareTo(ZERO) != 0) {
            int adjustmentIndex = -1;
            if (direction.compareTo(ZERO) >= 0) {
                for (int index = 0; index < references.length; index++) {
                    if (references[index].compareTo(ZERO) != 0) {
                        adjustmentIndex = index;
                        break;
                    }
                }
                if (adjustmentIndex == -1) {
                    adjustmentIndex = 0;
                }
            } else {
                for (int index = distributed.length - 1; index >= 0; index--) {
                    if (references[index].compareTo(ZERO) != 0) {
                        adjustmentIndex = index;
                        break;
                    }
                }
                if (adjustmentIndex == -1) {
                    adjustmentIndex = distributed.length - 1;
                }
            }
            distributed[adjustmentIndex] = distributed[adjustmentIndex].add(difference);
        }
        return List.of(distributed);
    }

    private static BigDecimal positiveDistribution(
            BigDecimal value,
            BigDecimal target,
            BigDecimal limit,
            int index,
            BigDecimal[] distributed) {
        if (target.compareTo(limit) >= 0) {
            distributed[index] = target;
            return value;
        }
        BigDecimal capacity = limit.subtract(target);
        if (value.compareTo(capacity) <= 0) {
            distributed[index] = target.add(value);
            return ZERO.setScale(value.scale(), HALF_EVEN);
        }
        distributed[index] = limit;
        return value.subtract(capacity);
    }

    private static BigDecimal negativeDistribution(
            BigDecimal value,
            BigDecimal target,
            BigDecimal limit,
            int index,
            BigDecimal[] distributed) {
        if (target.compareTo(limit) <= 0) {
            distributed[index] = target;
            return value;
        }
        BigDecimal capacity = target.subtract(limit);
        if (value.compareTo(capacity) <= 0) {
            distributed[index] = target.subtract(value);
            return ZERO.setScale(value.scale(), HALF_EVEN);
        }
        distributed[index] = limit;
        return value.subtract(capacity);
    }

    static BigDecimal sin(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.sin(value, mathContext);
    }

    static BigDecimal cos(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.cos(value, mathContext);
    }

    static BigDecimal tan(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.tan(value, mathContext);
    }

    static BigDecimal asin(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.asin(value, mathContext);
    }

    static BigDecimal acos(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.acos(value, mathContext);
    }

    static BigDecimal atan(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.atan(value, mathContext);
    }

    static BigDecimal atan2(MathContext mathContext, BigDecimal y, BigDecimal x) {
        return BigDecimalMath.atan2(y, x, mathContext);
    }

    static BigDecimal sinh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.sinh(value, mathContext);
    }

    static BigDecimal cosh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.cosh(value, mathContext);
    }

    static BigDecimal tanh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.tanh(value, mathContext);
    }

    static BigDecimal asinh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.asinh(value, mathContext);
    }

    static BigDecimal acosh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.acosh(value, mathContext);
    }

    static BigDecimal atanh(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.atanh(value, mathContext);
    }

    static BigDecimal ln(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.log(value, mathContext);
    }

    static BigDecimal lb(MathContext mathContext, BigDecimal value) {
        return BigDecimalMath.log2(value, mathContext);
    }

    static BigDecimal log(MathContext mathContext, BigDecimal base, BigDecimal value) {
        return BigDecimalMath.log(value, mathContext).divide(BigDecimalMath.log(base, mathContext), mathContext);
    }

    static BigDecimal lnFast(BigDecimal value) {
        return BigDecimal.valueOf(Math.log(value.doubleValue()));
    }

    static BigDecimal lbFast(BigDecimal value) {
        return BigDecimal.valueOf(Math.log(value.doubleValue()) / LN_2);
    }

    static BigDecimal logFast(BigDecimal base, BigDecimal value) {
        return BigDecimal.valueOf(Math.log(value.doubleValue()) / Math.log(base.doubleValue()));
    }

    static String concat(List<?> values) {
        Objects.requireNonNull(values, "values");
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            builder.append(requireString(value, "value"));
        }
        return builder.toString();
    }

    static String toUpper(String value) {
        return requireText(value).toUpperCase(Locale.ROOT);
    }

    static String toLower(String value) {
        return requireText(value).toLowerCase(Locale.ROOT);
    }

    static String trim(String value) {
        return requireText(value).strip();
    }

    static String trimLeft(String value) {
        return requireText(value).stripLeading();
    }

    static String trimRight(String value) {
        return requireText(value).stripTrailing();
    }

    static String substringFrom(String value, BigDecimal beginIndex) {
        return requireText(value).substring(integer(beginIndex));
    }

    static String substringBetween(String value, BigDecimal beginIndex, BigDecimal endIndex) {
        return requireText(value).substring(integer(beginIndex), integer(endIndex));
    }

    static String substringBefore(String value, String separator) {
        String text = requireText(value);
        String token = requireString(separator, "separator");
        if (token.isEmpty()) {
            return "";
        }
        int index = text.indexOf(token);
        return index >= 0 ? text.substring(0, index) : text;
    }

    static String substringAfter(String value, String separator) {
        String text = requireText(value);
        String token = requireString(separator, "separator");
        if (token.isEmpty()) {
            return text;
        }
        int index = text.indexOf(token);
        return index >= 0 ? text.substring(index + token.length()) : "";
    }

    static String substringBeforeLast(String value, String separator) {
        String text = requireText(value);
        String token = requireString(separator, "separator");
        if (token.isEmpty()) {
            return text;
        }
        int index = text.lastIndexOf(token);
        return index >= 0 ? text.substring(0, index) : text;
    }

    static String substringAfterLast(String value, String separator) {
        String text = requireText(value);
        String token = requireString(separator, "separator");
        if (token.isEmpty()) {
            return "";
        }
        int index = text.lastIndexOf(token);
        return index >= 0 ? text.substring(index + token.length()) : "";
    }

    static String padLeft(String value, BigDecimal size) {
        return padLeftWith(value, size, " ");
    }

    static String padLeftWith(String value, BigDecimal size, String padding) {
        return pad(requireText(value), integer(size), requirePadding(padding), true);
    }

    static String padRight(String value, BigDecimal size) {
        return padRightWith(value, size, " ");
    }

    static String padRightWith(String value, BigDecimal size, String padding) {
        return pad(requireText(value), integer(size), requirePadding(padding), false);
    }

    static String repeat(String value, BigDecimal times) {
        return requireText(value).repeat(integer(times));
    }

    static String replace(String value, String target, String replacement) {
        return requireText(value).replace(requireString(target, "target"), requireString(replacement, "replacement"));
    }

    static String replaceFirst(String value, String target, String replacement) {
        String text = requireText(value);
        String token = requireString(target, "target");
        String replacementText = requireString(replacement, "replacement");
        if (token.isEmpty()) {
            return replacementText + text;
        }
        int index = text.indexOf(token);
        if (index < 0) {
            return text;
        }
        return text.substring(0, index) + replacementText + text.substring(index + token.length());
    }

    static String replaceAll(String value, String regex, String replacement) {
        return Pattern.compile(requireString(regex, "regex"))
                .matcher(requireText(value))
                .replaceAll(requireString(replacement, "replacement"));
    }

    static BigDecimal indexOf(String value, String token) {
        return BigDecimal.valueOf(requireText(value).indexOf(requireString(token, "token")));
    }

    static BigDecimal lastIndexOf(String value, String token) {
        return BigDecimal.valueOf(requireText(value).lastIndexOf(requireString(token, "token")));
    }

    static Boolean startsWith(String value, String prefix) {
        return requireText(value).startsWith(requireString(prefix, "prefix"));
    }

    static Boolean endsWith(String value, String suffix) {
        return requireText(value).endsWith(requireString(suffix, "suffix"));
    }

    static Boolean contains(String value, String token) {
        return requireText(value).contains(requireString(token, "token"));
    }

    static Boolean isEmpty(String value) {
        return requireText(value).isEmpty();
    }

    static Boolean isBlank(String value) {
        return requireText(value).isBlank();
    }

    static BigDecimal length(String value) {
        return BigDecimal.valueOf(requireText(value).length());
    }

    static List<String> split(String value, String regex) {
        return List.of(Pattern.compile(requireString(regex, "regex")).split(requireText(value), -1));
    }

    static String join(List<?> values, String delimiter) {
        Objects.requireNonNull(values, "values");
        StringJoiner joiner = new StringJoiner(requireString(delimiter, "delimiter"));
        for (Object value : values) {
            joiner.add(String.valueOf(value));
        }
        return joiner.toString();
    }

    static BigDecimal secondsBetweenTime(LocalTime first, LocalTime second) {
        return BigDecimal.valueOf(ChronoUnit.SECONDS.between(first, second));
    }

    static BigDecimal secondsBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.SECONDS.between(first, second));
    }

    static BigDecimal minutesBetweenTime(LocalTime first, LocalTime second) {
        return BigDecimal.valueOf(ChronoUnit.MINUTES.between(first, second));
    }

    static BigDecimal minutesBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.MINUTES.between(first, second));
    }

    static BigDecimal hoursBetweenTime(LocalTime first, LocalTime second) {
        return BigDecimal.valueOf(ChronoUnit.HOURS.between(first, second));
    }

    static BigDecimal hoursBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.HOURS.between(first, second));
    }

    static BigDecimal daysBetweenDate(LocalDate first, LocalDate second) {
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(first, second));
    }

    static BigDecimal daysBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.DAYS.between(first, second));
    }

    static BigDecimal monthsBetweenDate(LocalDate first, LocalDate second) {
        return BigDecimal.valueOf(ChronoUnit.MONTHS.between(first, second));
    }

    static BigDecimal monthsBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.MONTHS.between(first, second));
    }

    static BigDecimal yearsBetweenDate(LocalDate first, LocalDate second) {
        return BigDecimal.valueOf(ChronoUnit.YEARS.between(first, second));
    }

    static BigDecimal yearsBetweenDateTime(LocalDateTime first, LocalDateTime second) {
        return BigDecimal.valueOf(ChronoUnit.YEARS.between(first, second));
    }

    static LocalDate setDayDate(LocalDate value, BigDecimal day) {
        return value.withDayOfMonth(integer(day));
    }

    static LocalDateTime setDayDateTime(LocalDateTime value, BigDecimal day) {
        return value.withDayOfMonth(integer(day));
    }

    static LocalDate setMonthDate(LocalDate value, BigDecimal month) {
        return value.withMonth(integer(month));
    }

    static LocalDateTime setMonthDateTime(LocalDateTime value, BigDecimal month) {
        return value.withMonth(integer(month));
    }

    static LocalDate setYearDate(LocalDate value, BigDecimal year) {
        return value.withYear(integer(year));
    }

    static LocalDateTime setYearDateTime(LocalDateTime value, BigDecimal year) {
        return value.withYear(integer(year));
    }

    static LocalTime setHoursTime(LocalTime value, BigDecimal hour) {
        return value.withHour(integer(hour));
    }

    static LocalDateTime setHoursDateTime(LocalDateTime value, BigDecimal hour) {
        return value.withHour(integer(hour));
    }

    static LocalTime setMinutesTime(LocalTime value, BigDecimal minute) {
        return value.withMinute(integer(minute));
    }

    static LocalDateTime setMinutesDateTime(LocalDateTime value, BigDecimal minute) {
        return value.withMinute(integer(minute));
    }

    static LocalTime setSecondsTime(LocalTime value, BigDecimal second) {
        return value.withSecond(integer(second));
    }

    static LocalDateTime setSecondsDateTime(LocalDateTime value, BigDecimal second) {
        return value.withSecond(integer(second));
    }

    static LocalTime setMidnightTime(LocalTime ignored) {
        return LocalTime.MIDNIGHT;
    }

    static LocalDateTime setMidnightDateTime(LocalDateTime value) {
        return value.with(LocalTime.MIDNIGHT);
    }

    static LocalTime setMiddayTime(LocalTime ignored) {
        return LocalTime.NOON;
    }

    static LocalDateTime setMiddayDateTime(LocalDateTime value) {
        return value.with(LocalTime.NOON);
    }

    static LocalDate addDayDate(LocalDate value, BigDecimal amount) {
        return value.plusDays(longValue(amount));
    }

    static LocalDateTime addDayDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusDays(longValue(amount));
    }

    static LocalDate addMonthDate(LocalDate value, BigDecimal amount) {
        return value.plusMonths(longValue(amount));
    }

    static LocalDateTime addMonthDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusMonths(longValue(amount));
    }

    static LocalDate addYearDate(LocalDate value, BigDecimal amount) {
        return value.plusYears(longValue(amount));
    }

    static LocalDateTime addYearDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusYears(longValue(amount));
    }

    static LocalTime addHoursTime(LocalTime value, BigDecimal amount) {
        return value.plusHours(longValue(amount));
    }

    static LocalDateTime addHoursDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusHours(longValue(amount));
    }

    static LocalTime addMinutesTime(LocalTime value, BigDecimal amount) {
        return value.plusMinutes(longValue(amount));
    }

    static LocalDateTime addMinutesDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusMinutes(longValue(amount));
    }

    static LocalTime addSecondsTime(LocalTime value, BigDecimal amount) {
        return value.plusSeconds(longValue(amount));
    }

    static LocalDateTime addSecondsDateTime(LocalDateTime value, BigDecimal amount) {
        return value.plusSeconds(longValue(amount));
    }

    static LocalDate subDayDate(LocalDate value, BigDecimal amount) {
        return value.minusDays(longValue(amount));
    }

    static LocalDateTime subDayDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusDays(longValue(amount));
    }

    static LocalDate subMonthDate(LocalDate value, BigDecimal amount) {
        return value.minusMonths(longValue(amount));
    }

    static LocalDateTime subMonthDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusMonths(longValue(amount));
    }

    static LocalDate subYearDate(LocalDate value, BigDecimal amount) {
        return value.minusYears(longValue(amount));
    }

    static LocalDateTime subYearDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusYears(longValue(amount));
    }

    static LocalTime subHoursTime(LocalTime value, BigDecimal amount) {
        return value.minusHours(longValue(amount));
    }

    static LocalDateTime subHoursDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusHours(longValue(amount));
    }

    static LocalTime subMinutesTime(LocalTime value, BigDecimal amount) {
        return value.minusMinutes(longValue(amount));
    }

    static LocalDateTime subMinutesDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusMinutes(longValue(amount));
    }

    static LocalTime subSecondsTime(LocalTime value, BigDecimal amount) {
        return value.minusSeconds(longValue(amount));
    }

    static LocalDateTime subSecondsDateTime(LocalDateTime value, BigDecimal amount) {
        return value.minusSeconds(longValue(amount));
    }

    static BigDecimal maxNumber(List<?> values) {
        return max(cast(values, BigDecimal.class));
    }

    static BigDecimal minNumber(List<?> values) {
        return min(cast(values, BigDecimal.class));
    }

    static String maxString(List<?> values) {
        return max(cast(values, String.class));
    }

    static String minString(List<?> values) {
        return min(cast(values, String.class));
    }

    static LocalDate maxDate(List<?> values) {
        return max(cast(values, LocalDate.class));
    }

    static LocalDate minDate(List<?> values) {
        return min(cast(values, LocalDate.class));
    }

    static LocalTime maxTime(List<?> values) {
        return max(cast(values, LocalTime.class));
    }

    static LocalTime minTime(List<?> values) {
        return min(cast(values, LocalTime.class));
    }

    static LocalDateTime maxDateTime(List<?> values) {
        return max(cast(values, LocalDateTime.class));
    }

    static LocalDateTime minDateTime(List<?> values) {
        return min(cast(values, LocalDateTime.class));
    }

    static BigDecimal fvRegular(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal payment,
            BigDecimal presentValue,
            Boolean dueAtPeriodStart) {
        if (rate.compareTo(ZERO) == 0) {
            return presentValue.add(periods.multiply(payment)).negate();
        }
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        return ONE.subtract(ratePowerPeriods)
                .multiply(dueAtPeriodStart ? ratePlusOne : ONE)
                .multiply(payment)
                .divide(rate, mathContext)
                .subtract(presentValue.multiply(ratePowerPeriods));
    }

    static BigDecimal pv(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal payment,
            BigDecimal futureValue,
            Boolean dueAtPeriodStart) {
        if (rate.compareTo(ZERO) == 0) {
            return periods.multiply(payment).add(futureValue).negate();
        }
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        return ONE.subtract(ratePowerPeriods)
                .divide(rate, mathContext)
                .multiply(dueAtPeriodStart ? ratePlusOne : ONE)
                .multiply(payment)
                .subtract(futureValue)
                .divide(ratePowerPeriods, mathContext);
    }

    static BigDecimal npv(MathContext mathContext, BigDecimal rate, List<?> cashFlows) {
        BigDecimal npv = ZERO;
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal currentRate = ratePlusOne;
        for (BigDecimal cashFlow : numbers(cashFlows)) {
            npv = npv.add(cashFlow.divide(currentRate, mathContext));
            currentRate = currentRate.multiply(ratePlusOne);
        }
        return npv;
    }

    static BigDecimal pmtRegular(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue,
            Boolean dueAtPeriodStart) {
        if (rate.compareTo(ZERO) == 0) {
            return futureValue.add(presentValue).negate().divide(periods, mathContext);
        }
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        return futureValue.add(presentValue.multiply(ratePowerPeriods))
                .multiply(rate)
                .divide((dueAtPeriodStart ? ratePlusOne : ONE).multiply(ONE.subtract(ratePowerPeriods)), mathContext);
    }

    static BigDecimal nper(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal payment,
            BigDecimal presentValue,
            BigDecimal futureValue,
            Boolean dueAtPeriodStart) {
        if (rate.compareTo(ZERO) == 0) {
            return futureValue.add(presentValue).negate().divide(payment, mathContext);
        }
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePayment = (dueAtPeriodStart ? ratePlusOne : ONE).multiply(payment).divide(rate, mathContext);
        BigDecimal ratePaymentMinusFuture = ratePayment.subtract(futureValue);
        BigDecimal firstLog = BigDecimalMath.log(ratePaymentMinusFuture.abs(), mathContext);
        BigDecimal secondLog = ratePaymentMinusFuture.compareTo(ZERO) < 0
                ? BigDecimalMath.log(presentValue.add(ratePayment).negate(), mathContext)
                : BigDecimalMath.log(presentValue.add(ratePayment), mathContext);
        return firstLog.subtract(secondLog).divide(BigDecimalMath.log(ratePlusOne, mathContext), mathContext);
    }

    static BigDecimal pmtTyped(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue,
            BigDecimal type) {
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        BigDecimal numerator = rate.negate().multiply(presentValue.multiply(ratePowerPeriods).add(futureValue));
        BigDecimal denominator = ONE.add(rate.multiply(BigDecimal.valueOf(integer(type))))
                .multiply(ratePowerPeriods.subtract(ONE));
        return numerator.divide(denominator, mathContext);
    }

    static BigDecimal pmtNoType(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue) {
        return pmtTyped(mathContext, rate, periods, presentValue, futureValue, ZERO);
    }

    static BigDecimal pmtPresent(MathContext mathContext, BigDecimal rate, BigDecimal periods, BigDecimal presentValue) {
        return pmtNoType(mathContext, rate, periods, presentValue, ZERO);
    }

    static BigDecimal ipmtTyped(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue,
            BigDecimal type) {
        BigDecimal interestPayment = fvTyped(
                mathContext,
                rate,
                period.subtract(ONE),
                pmtTyped(mathContext, rate, periods, presentValue, futureValue, type),
                presentValue,
                type)
                .multiply(rate, mathContext);
        if (integer(type) == 1) {
            interestPayment = interestPayment.divide(ONE.add(rate), mathContext);
        }
        return interestPayment;
    }

    static BigDecimal ipmtNoType(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue) {
        return ipmtTyped(mathContext, rate, period, periods, presentValue, futureValue, ZERO);
    }

    static BigDecimal ipmtPresent(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue) {
        return ipmtNoType(mathContext, rate, period, periods, presentValue, ZERO);
    }

    static BigDecimal ppmtTyped(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue,
            BigDecimal type) {
        return pmtTyped(mathContext, rate, periods, presentValue, futureValue, type)
                .subtract(ipmtTyped(mathContext, rate, period, periods, presentValue, futureValue, type));
    }

    static BigDecimal ppmtNoType(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue,
            BigDecimal futureValue) {
        return pmtNoType(mathContext, rate, periods, presentValue, futureValue)
                .subtract(ipmtNoType(mathContext, rate, period, periods, presentValue, futureValue));
    }

    static BigDecimal ppmtPresent(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal period,
            BigDecimal periods,
            BigDecimal presentValue) {
        return pmtPresent(mathContext, rate, periods, presentValue)
                .subtract(ipmtPresent(mathContext, rate, period, periods, presentValue));
    }

    static BigDecimal fvTyped(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal payment,
            BigDecimal presentValue,
            BigDecimal type) {
        BigDecimal ratePlusOne = rate.add(ONE);
        BigDecimal ratePowerPeriods = BigDecimalMath.pow(ratePlusOne, periods, mathContext);
        return presentValue.multiply(ratePowerPeriods)
                .add(payment.multiply(ONE.add(rate.multiply(BigDecimal.valueOf(integer(type)))))
                        .multiply(ratePowerPeriods.subtract(ONE))
                        .divide(rate, mathContext))
                .negate();
    }

    static BigDecimal fvNoType(
            MathContext mathContext,
            BigDecimal rate,
            BigDecimal periods,
            BigDecimal payment,
            BigDecimal presentValue) {
        return fvTyped(mathContext, rate, periods, payment, presentValue, ZERO);
    }

    static BigDecimal asNumber(BoundaryCoercion boundaryCoercion, Object value) {
        return (BigDecimal) boundaryCoercion.convertFunctionBindingFallback(value, NUMBER);
    }

    static String asText(BoundaryCoercion boundaryCoercion, Object value) {
        return (String) boundaryCoercion.convertFunctionBindingFallback(value, STRING);
    }

    static Boolean asBool(BoundaryCoercion boundaryCoercion, Object value) {
        return (Boolean) boundaryCoercion.convertFunctionBindingFallback(value, BOOLEAN);
    }

    static LocalDate asDate(BoundaryCoercion boundaryCoercion, Object value) {
        return (LocalDate) boundaryCoercion.convertFunctionBindingFallback(value, DATE);
    }

    static LocalTime asTime(BoundaryCoercion boundaryCoercion, Object value) {
        return (LocalTime) boundaryCoercion.convertFunctionBindingFallback(value, TIME);
    }

    static LocalDateTime asDateTime(BoundaryCoercion boundaryCoercion, Object value) {
        return (LocalDateTime) boundaryCoercion.convertFunctionBindingFallback(value, DATETIME);
    }

    static List<Object> asVector(int materializationLimit, Object value) {
        if (value instanceof Map<?, ?>) {
            throw new IllegalArgumentException("asVector rejects map values as ambiguous");
        }
        ArrayList<Object> values;
        if (value instanceof Collection<?> collection) {
            if (collection.size() > materializationLimit) {
                throw new IllegalArgumentException("asVector materialization limit exceeded");
            }
            values = new ArrayList<>(collection.size());
            values.addAll(collection);
            return Collections.unmodifiableList(values);
        }
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            if (length > materializationLimit) {
                throw new IllegalArgumentException("asVector materialization limit exceeded");
            }
            values = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                values.add(Array.get(value, index));
            }
            return Collections.unmodifiableList(values);
        }
        throw new IllegalArgumentException("asVector requires a vector, collection, or array value");
    }

    private static BigDecimal[] numbers(List<?> values) {
        Objects.requireNonNull(values, "values");
        BigDecimal[] numbers = new BigDecimal[values.size()];
        for (int index = 0; index < values.size(); index++) {
            Object value = values.get(index);
            if (!(value instanceof BigDecimal number)) {
                throw new IllegalArgumentException("value at index " + index + " is not a number");
            }
            numbers[index] = number;
        }
        return numbers;
    }

    private static <T> List<T> cast(List<?> values, Class<T> valueType) {
        Objects.requireNonNull(values, "values");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("comparable vector must not be empty");
        }
        ArrayList<T> typedValues = new ArrayList<>(values.size());
        for (Object value : values) {
            typedValues.add(valueType.cast(value));
        }
        return typedValues;
    }

    private static <T extends Comparable<? super T>> T max(List<T> values) {
        T result = values.getFirst();
        for (int index = 1; index < values.size(); index++) {
            T value = values.get(index);
            if (result.compareTo(value) < 0) {
                result = value;
            }
        }
        return result;
    }

    private static <T extends Comparable<? super T>> T min(List<T> values) {
        T result = values.getFirst();
        for (int index = 1; index < values.size(); index++) {
            T value = values.get(index);
            if (result.compareTo(value) > 0) {
                result = value;
            }
        }
        return result;
    }

    private static int integer(BigDecimal value) {
        return value.intValueExact();
    }

    private static long longValue(BigDecimal value) {
        return value.longValueExact();
    }

    private static String pad(String value, int size, String padding, boolean leftAlignedPadding) {
        if (size <= value.length()) {
            return value;
        }
        String filler = repeatToLength(padding, size - value.length());
        return leftAlignedPadding ? filler + value : value + filler;
    }

    private static String repeatToLength(String padding, int size) {
        StringBuilder builder = new StringBuilder(size);
        while (builder.length() < size) {
            builder.append(padding);
        }
        if (builder.length() > size) {
            builder.setLength(size);
        }
        return builder.toString();
    }

    private static String requireText(String value) {
        return requireString(value, "value");
    }

    private static String requireString(Object value, String name) {
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException(name + " must be text");
        }
        return text;
    }

    private static String requirePadding(String padding) {
        String value = requireString(padding, "padding");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("padding must not be empty");
        }
        return value;
    }

    private static double kahanSum(BigDecimal[] values) {
        double sum = 0.0;
        double compensation = 0.0;
        for (BigDecimal value : values) {
            double corrected = value.doubleValue() - compensation;
            double next = sum + corrected;
            compensation = (next - sum) - corrected;
            sum = next;
        }
        return sum;
    }

    private static double kahanSumAbsoluteDeviations(BigDecimal[] values, double mean) {
        double sum = 0.0;
        double compensation = 0.0;
        for (BigDecimal value : values) {
            double corrected = Math.abs(value.doubleValue() - mean) - compensation;
            double next = sum + corrected;
            compensation = (next - sum) - corrected;
            sum = next;
        }
        return sum;
    }
}

enum BuiltInFunctionGroup {
    MATH(Set.of(
            "abs",
            "sqrt",
            "mean",
            "geometricMean",
            "harmonicMean",
            "variance",
            "stdDev",
            "meanDev",
            "rule3d",
            "rule3i",
            "distribute",
            "spread")),
    TRANSCENDENTAL(Set.of(
            "sin",
            "cos",
            "tan",
            "asin",
            "acos",
            "atan",
            "atan2",
            "sinh",
            "cosh",
            "tanh",
            "asinh",
            "acosh",
            "atanh",
            "ln",
            "lb",
            "log",
            "lnFast",
            "lbFast",
            "logFast")),
    STRING(Set.of(
            "concat",
            "toUpper",
            "toLower",
            "trim",
            "trimLeft",
            "trimRight",
            "substring",
            "substringBefore",
            "substringAfter",
            "substringBeforeLast",
            "substringAfterLast",
            "padLeft",
            "padRight",
            "repeat",
            "replace",
            "replaceFirst",
            "replaceAll",
            "indexOf",
            "lastIndexOf",
            "startsWith",
            "endsWith",
            "contains",
            "isEmpty",
            "isBlank",
            "length",
            "split",
            "join")),
    DATE_TIME(Set.of(
            "secondsBetween",
            "minutesBetween",
            "hoursBetween",
            "daysBetween",
            "monthsBetween",
            "yearsBetween",
            "setDay",
            "setMonth",
            "setYear",
            "setHours",
            "setMinutes",
            "setSeconds",
            "setMidnight",
            "setMidday",
            "addDay",
            "addMonth",
            "addYear",
            "addHours",
            "addMinutes",
            "addSeconds",
            "subDay",
            "subMonth",
            "subYear",
            "subHours",
            "subMinutes",
            "subSeconds")),
    COMPARABLE(Set.of("max", "min")),
    FINANCIAL(Set.of("fv", "pv", "npv", "pmt", "nper", "ipmt", "ppmt")),
    ASSERTION(Set.of("asNumber", "asText", "asBool", "asDate", "asTime", "asDateTime", "asVector"));

    private final Set<String> languageNames;

    BuiltInFunctionGroup(Set<String> languageNames) {
        this.languageNames = languageNames;
    }

    Set<String> languageNames() {
        return languageNames;
    }
}
