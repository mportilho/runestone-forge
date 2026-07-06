package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class StandardBuiltIns {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final ExpressionType NUMBER_VECTOR = new VectorType(ScalarType.NUMBER);
    private static final ExpressionType STRING_VECTOR = new VectorType(ScalarType.STRING);
    private static final ExpressionType UNKNOWN_VECTOR = new VectorType(UnknownType.INSTANCE);
    private static final ExpressionType UNKNOWN_COLLECTION = new CollectionType(UnknownType.INSTANCE);

    private StandardBuiltIns() {
    }

    static void registerStandardGroups(
            FunctionCatalog.Builder catalog,
            MathContext mathContext,
            MathContext transcendentalMathContext,
            BoundaryCoercion boundaryCoercion,
            int materializationLimit) {
        for (StandardFunctionGroup group : StandardFunctionGroup.values()) {
            for (FunctionDescriptor descriptor : groupDescriptors(
                    group,
                    mathContext,
                    transcendentalMathContext,
                    boundaryCoercion,
                    materializationLimit)) {
                catalog.register(descriptor);
            }
        }
    }

    static List<FunctionDescriptor> groupDescriptors(
            StandardFunctionGroup group,
            MathContext mathContext,
            MathContext transcendentalMathContext,
            BoundaryCoercion boundaryCoercion,
            int materializationLimit) {
        Objects.requireNonNull(group, "group");
        MathContext effectiveMathContext = group == StandardFunctionGroup.TRANSCENDENTAL
                ? transcendentalMathContext
                : mathContext;
        return importPublicStaticMethods(providerClass(group), effectiveMathContext, boundaryCoercion, materializationLimit);
    }

    static Set<String> requiredFunctionNames(StandardFunctionGroup group) {
        return groupDescriptors(
                group,
                MathContext.DECIMAL128,
                MathContext.DECIMAL128,
                BoundaryCoercion.standard(),
                1)
                .stream()
                .map(FunctionDescriptor::languageName)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    static void validateGroup(StandardFunctionGroup group, Collection<FunctionDescriptor> descriptors) {
        Objects.requireNonNull(group, "group");
        Objects.requireNonNull(descriptors, "descriptors");
        Set<String> registeredNames = descriptors.stream()
                .map(StandardBuiltIns::contract)
                .map(FunctionContract::canonical)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        ArrayList<String> missing = new ArrayList<>();
        for (FunctionContract requiredFunction : requiredFunctionContracts(group)) {
            if (!registeredNames.contains(requiredFunction.canonical())) {
                missing.add(requiredFunction.canonical());
            }
        }
        missing.sort(Comparator.naturalOrder());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("standard function group " + group + " is incomplete; missing " + missing);
        }
    }

    private static Set<FunctionContract> requiredFunctionContracts(StandardFunctionGroup group) {
        return groupDescriptors(
                group,
                MathContext.DECIMAL128,
                MathContext.DECIMAL128,
                BoundaryCoercion.standard(),
                1)
                .stream()
                .map(StandardBuiltIns::contract)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static Class<?> providerClass(StandardFunctionGroup group) {
        return switch (group) {
            case MATH -> StandardMathFunctions.class;
            case TRANSCENDENTAL -> StandardTranscendentalFunctions.class;
            case STRING -> StandardStringFunctions.class;
            case DATE_TIME -> StandardDateTimeFunctions.class;
            case COMPARABLE -> StandardComparableFunctions.class;
            case FINANCIAL -> StandardFinancialFunctions.class;
            case ASSERTION -> StandardAssertionFunctions.class;
        };
    }

    private static List<FunctionDescriptor> importPublicStaticMethods(
            Class<?> providerClass,
            MathContext mathContext,
            BoundaryCoercion boundaryCoercion,
            int materializationLimit) {
        Method[] methods = providerClass.getDeclaredMethods();
        java.util.Arrays.sort(methods, Comparator
                .comparing(Method::getName)
                .thenComparingInt(Method::getParameterCount)
                .thenComparing(Method::toGenericString));
        ArrayList<FunctionDescriptor> descriptors = new ArrayList<>();
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isStatic(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }
            descriptors.add(toDescriptor(method, mathContext, boundaryCoercion, materializationLimit));
        }
        return List.copyOf(descriptors);
    }

    private static FunctionDescriptor toDescriptor(
            Method method,
            MathContext mathContext,
            BoundaryCoercion boundaryCoercion,
            int materializationLimit) {
        try {
            MethodHandle handle = LOOKUP.unreflect(method);
            Class<?>[] methodParameterTypes = method.getParameterTypes();
            int firstLanguageParameter = 0;
            while (firstLanguageParameter < methodParameterTypes.length) {
                Class<?> parameterType = methodParameterTypes[firstLanguageParameter];
                if (parameterType == MathContext.class) {
                    handle = MethodHandles.insertArguments(handle, 0, mathContext);
                } else if (parameterType == BoundaryCoercion.class) {
                    handle = MethodHandles.insertArguments(handle, 0, boundaryCoercion);
                } else if (parameterType == int.class && method.getDeclaringClass() == StandardAssertionFunctions.class) {
                    handle = MethodHandles.insertArguments(handle, 0, materializationLimit);
                } else {
                    break;
                }
                firstLanguageParameter++;
            }

            ArrayList<ExpressionType> parameterTypes = new ArrayList<>(methodParameterTypes.length - firstLanguageParameter);
            for (int index = firstLanguageParameter; index < methodParameterTypes.length; index++) {
                parameterTypes.add(expressionType(method.getName(), methodParameterTypes[index], false));
            }

            boolean deterministicAssertion = method.getDeclaringClass() != StandardAssertionFunctions.class
                    || boundaryCoercion.deterministicForConstants();
            FunctionDescriptor.Builder builder = FunctionDescriptor.builder(method.getName())
                    .parameterTypes(parameterTypes)
                    .returnType(expressionType(method.getName(), method.getReturnType(), true))
                    .implementationHandle(handle, method.getDeclaringClass().getName() + '.' + method.getName());
            if (deterministicAssertion) {
                builder.pure().foldable();
            }
            return builder.build();
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("function implementation method is not accessible: " + method, exception);
        }
    }

    private static ExpressionType expressionType(String methodName, Class<?> javaType, boolean returnType) {
        if (javaType == BigDecimal.class || Number.class.isAssignableFrom(javaType)
                || javaType.isPrimitive() && javaType != boolean.class && javaType != char.class) {
            return ScalarType.NUMBER;
        }
        if (javaType == Boolean.class || javaType == boolean.class) {
            return ScalarType.BOOLEAN;
        }
        if (javaType == String.class) {
            return ScalarType.STRING;
        }
        if (javaType == LocalDate.class) {
            return ScalarType.DATE;
        }
        if (javaType == LocalTime.class) {
            return ScalarType.TIME;
        }
        if (javaType == LocalDateTime.class) {
            return ScalarType.DATETIME;
        }
        if (javaType == BigDecimal[].class) {
            return NUMBER_VECTOR;
        }
        if (javaType == String[].class) {
            return STRING_VECTOR;
        }
        if (javaType.isArray()) {
            return UNKNOWN_VECTOR;
        }
        if (javaType == List.class || Collection.class.isAssignableFrom(javaType)) {
            return methodName.equals("asVector") && returnType ? UNKNOWN_VECTOR : UNKNOWN_COLLECTION;
        }
        if (javaType == Object.class || Temporal.class.isAssignableFrom(javaType) || Comparable.class.isAssignableFrom(javaType)) {
            return UnknownType.INSTANCE;
        }
        throw new IllegalArgumentException("unsupported function " + (returnType ? "return" : "parameter")
                + " type for " + methodName + ": " + javaType.getName());
    }

    private static FunctionContract contract(FunctionDescriptor descriptor) {
        return new FunctionContract(
                descriptor.languageName(),
                descriptor.parameterTypes(),
                descriptor.returnType(),
                descriptor.isPure(),
                descriptor.isFoldable());
    }

    private record FunctionContract(
            String languageName,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType,
            boolean pure,
            boolean foldable) {

        private String canonical() {
            StringBuilder canonical = new StringBuilder(languageName).append('(');
            for (int index = 0; index < parameterTypes.size(); index++) {
                if (index > 0) {
                    canonical.append(',');
                }
                canonical.append(ExpressionTypes.canonical(parameterTypes.get(index)));
            }
            return canonical.append("):")
                    .append(ExpressionTypes.canonical(returnType))
                    .append(":pure=")
                    .append(pure)
                    .append(":foldable=")
                    .append(foldable)
                    .toString();
        }
    }
}
