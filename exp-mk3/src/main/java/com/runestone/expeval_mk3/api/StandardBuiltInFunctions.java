package com.runestone.expeval_mk3.api;

import java.math.MathContext;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.runestone.expeval_mk3.api.ScalarType.BOOLEAN;
import static com.runestone.expeval_mk3.api.ScalarType.DATE;
import static com.runestone.expeval_mk3.api.ScalarType.DATETIME;
import static com.runestone.expeval_mk3.api.ScalarType.NUMBER;
import static com.runestone.expeval_mk3.api.ScalarType.STRING;
import static com.runestone.expeval_mk3.api.ScalarType.TIME;

final class StandardBuiltInFunctions {

    private static final ExpressionType NUMBER_COLLECTION = new CollectionType(NUMBER);
    private static final ExpressionType STRING_COLLECTION = new CollectionType(STRING);

    private StandardBuiltInFunctions() {
    }

    static void registerAll(
            FunctionCatalog.Builder functions,
            JavaTypeCatalog javaTypes,
            BoundaryCoercion boundaryCoercion,
            MathContext mathContext,
            MathContext transcendentalMathContext,
            int maxMaterializedSize) {
        Objects.requireNonNull(functions, "functions");
        Objects.requireNonNull(mathContext, "mathContext");
        Objects.requireNonNull(transcendentalMathContext, "transcendentalMathContext");
        BuiltInResolutionContext context = new BuiltInResolutionContext(javaTypes, boundaryCoercion, maxMaterializedSize);

        register(functions, BuiltInFunctionGroup.MATH, MathBuiltInFunctions.descriptors(context, mathContext));
        register(functions, BuiltInFunctionGroup.TRANSCENDENTAL,
                TranscendentalBuiltInFunctions.descriptors(context, transcendentalMathContext));
        register(functions, BuiltInFunctionGroup.STRING, StringBuiltInFunctions.descriptors(context));
        register(functions, BuiltInFunctionGroup.DATE_TIME, DateTimeBuiltInFunctions.descriptors(context));
        register(functions, BuiltInFunctionGroup.COMPARABLE, ComparableBuiltInFunctions.descriptors(context));
        register(functions, BuiltInFunctionGroup.FINANCIAL, FinancialBuiltInFunctions.descriptors(context, mathContext));
        register(functions, BuiltInFunctionGroup.ASSERTION, AssertionBuiltInFunctions.descriptors(boundaryCoercion));
    }

    private static void register(
            FunctionCatalog.Builder functions,
            BuiltInFunctionGroup group,
            List<FunctionDescriptor> descriptors) {
        validateGroup(group, descriptors);
        Set<FunctionSignature> actualSignatures = descriptors.stream()
                .map(FunctionDescriptor::signature)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        Set<FunctionSignature> expectedSignatures = expectedSignatures(group);
        if (!actualSignatures.equals(expectedSignatures)) {
            throw new IllegalStateException(group + " built-in function group contains unexpected signatures");
        }
        for (FunctionDescriptor descriptor : descriptors) {
            functions.registerOfficial(descriptor);
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
                    signature("mean", NUMBER_COLLECTION),
                    signature("geometricMean", NUMBER_COLLECTION),
                    signature("harmonicMean", NUMBER_COLLECTION),
                    signature("variance", NUMBER_COLLECTION, NUMBER),
                    signature("stdDev", NUMBER_COLLECTION, NUMBER),
                    signature("meanDev", NUMBER_COLLECTION),
                    signature("rule3d", NUMBER, NUMBER, NUMBER),
                    signature("rule3i", NUMBER, NUMBER, NUMBER),
                    signature("distribute", NUMBER, NUMBER, NUMBER_COLLECTION, NUMBER_COLLECTION),
                    signature("spread", NUMBER, NUMBER, NUMBER_COLLECTION));
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
                    signature("concat", STRING_COLLECTION),
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
                    signature("join", STRING_COLLECTION, STRING));
            case DATE_TIME -> dateTimeExpectedSignatures();
            case COMPARABLE -> signatures(
                    signature("max", new CollectionType(NUMBER)),
                    signature("min", new CollectionType(NUMBER)),
                    signature("max", new CollectionType(STRING)),
                    signature("min", new CollectionType(STRING)),
                    signature("max", new CollectionType(DATE)),
                    signature("min", new CollectionType(DATE)),
                    signature("max", new CollectionType(TIME)),
                    signature("min", new CollectionType(TIME)),
                    signature("max", new CollectionType(DATETIME)),
                    signature("min", new CollectionType(DATETIME)));
            case FINANCIAL -> signatures(
                    signature("fv", NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN),
                    signature("pv", NUMBER, NUMBER, NUMBER, NUMBER, BOOLEAN),
                    signature("npv", NUMBER, NUMBER_COLLECTION),
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
            case ASSERTION -> assertionExpectedSignatures();
        };
    }

    private static Set<FunctionSignature> assertionExpectedSignatures() {
        java.util.LinkedHashSet<FunctionSignature> signatures = new java.util.LinkedHashSet<>();
        addAssertionSignatures(signatures, "asNumber");
        addAssertionSignatures(signatures, "asText");
        addAssertionSignatures(signatures, "asBool");
        addAssertionSignatures(signatures, "asDate");
        addAssertionSignatures(signatures, "asTime");
        addAssertionSignatures(signatures, "asDateTime");
        return Set.copyOf(signatures);
    }

    private static void addAssertionSignatures(java.util.Set<FunctionSignature> signatures, String languageName) {
        add(signatures, languageName, NUMBER);
        add(signatures, languageName, BOOLEAN);
        add(signatures, languageName, STRING);
        add(signatures, languageName, DATE);
        add(signatures, languageName, TIME);
        add(signatures, languageName, DATETIME);
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
    ASSERTION(Set.of("asNumber", "asText", "asBool", "asDate", "asTime", "asDateTime"));

    private final Set<String> languageNames;

    BuiltInFunctionGroup(Set<String> languageNames) {
        this.languageNames = languageNames;
    }

    Set<String> languageNames() {
        return languageNames;
    }
}
