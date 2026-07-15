package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class FunctionHandleAdapters {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final MethodHandle REQUIRE_NON_NULL = find(
            "requireNonNull", MethodType.methodType(Object.class, Object.class));
    private static final Map<Class<?>, NumericAdapter> NUMERIC_ADAPTERS = Map.ofEntries(
            numeric(byte.class, "toByte", "fromByte"),
            numeric(Byte.class, "toByteBox", "fromByteBox"),
            numeric(short.class, "toShort", "fromShort"),
            numeric(Short.class, "toShortBox", "fromShortBox"),
            numeric(int.class, "toInt", "fromInt"),
            numeric(Integer.class, "toInteger", "fromInteger"),
            numeric(long.class, "toLong", "fromLong"),
            numeric(Long.class, "toLongBox", "fromLongBox"),
            numeric(float.class, "toFloat", "fromFloat"),
            numeric(Float.class, "toFloatBox", "fromFloatBox"),
            numeric(double.class, "toDouble", "fromDouble"),
            numeric(Double.class, "toDoubleBox", "fromDoubleBox"));

    private FunctionHandleAdapters() {
    }

    static MethodHandle adapt(
            MethodHandle implementationHandle,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType) {
        Objects.requireNonNull(implementationHandle, "implementationHandle");
        parameterTypes = ExpressionTypes.copyOf(parameterTypes, "parameterTypes");
        Objects.requireNonNull(returnType, "returnType");
        if (implementationHandle.type().parameterCount() != parameterTypes.size()) {
            throw new IllegalArgumentException("implementation handle arity must match parameter types");
        }

        MethodHandle adapted = adaptNumericArguments(implementationHandle, parameterTypes);
        adapted = adaptNumericReturn(adapted, returnType);
        return adapted.asType(canonicalMethodType(parameterTypes, returnType));
    }

    static MethodHandle adaptInstance(
            MethodHandle implementationHandle,
            Class<?> receiverType,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType) {
        Objects.requireNonNull(implementationHandle, "implementationHandle");
        Objects.requireNonNull(receiverType, "receiverType");
        parameterTypes = ExpressionTypes.copyOf(parameterTypes, "parameterTypes");
        Objects.requireNonNull(returnType, "returnType");
        if (implementationHandle.type().parameterCount() != parameterTypes.size() + 1) {
            throw new IllegalArgumentException("implementation handle arity must match receiver plus parameter types");
        }

        MethodHandle adapted = adaptNumericArguments(implementationHandle, parameterTypes, 1);
        adapted = adaptNumericReturn(adapted, returnType);
        return adapted.asType(canonicalInstanceMethodType(receiverType, parameterTypes, returnType));
    }

    static Object adaptNumericValue(BigDecimal value, Class<?> targetType) {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(targetType, "targetType");
        if (targetType == BigDecimal.class) {
            return value;
        }
        if (targetType == byte.class || targetType == Byte.class) {
            return toByte(value);
        }
        if (targetType == short.class || targetType == Short.class) {
            return toShort(value);
        }
        if (targetType == int.class || targetType == Integer.class) {
            return toInt(value);
        }
        if (targetType == long.class || targetType == Long.class) {
            return toLong(value);
        }
        if (targetType == float.class || targetType == Float.class) {
            return toFloat(value);
        }
        if (targetType == double.class || targetType == Double.class) {
            return toDouble(value);
        }
        return value;
    }

    static MethodHandle guardNonNullBoundaries(MethodHandle implementationHandle) {
        Objects.requireNonNull(implementationHandle, "implementationHandle");
        MethodHandle guarded = implementationHandle;
        for (int index = 0; index < guarded.type().parameterCount(); index++) {
            Class<?> parameterType = guarded.type().parameterType(index);
            guarded = MethodHandles.filterArguments(
                    guarded, index, REQUIRE_NON_NULL.asType(MethodType.methodType(parameterType, parameterType)));
        }
        Class<?> returnType = guarded.type().returnType();
        return MethodHandles.filterReturnValue(
                guarded, REQUIRE_NON_NULL.asType(MethodType.methodType(returnType, returnType)));
    }

    private static MethodHandle adaptNumericArguments(MethodHandle implementationHandle, List<ExpressionType> parameterTypes) {
        return adaptNumericArguments(implementationHandle, parameterTypes, 0);
    }

    private static MethodHandle adaptNumericArguments(
            MethodHandle implementationHandle,
            List<ExpressionType> parameterTypes,
            int parameterOffset) {
        MethodHandle adapted = implementationHandle;
        for (int index = 0; index < parameterTypes.size(); index++) {
            if (parameterTypes.get(index) == ScalarType.NUMBER) {
                Class<?> targetType = adapted.type().parameterType(index + parameterOffset);
                if (targetType != BigDecimal.class) {
                    adapted = MethodHandles.filterArguments(adapted, index + parameterOffset, bigDecimalToNumeric(targetType));
                }
            }
        }
        return adapted;
    }

    private static MethodHandle adaptNumericReturn(MethodHandle implementationHandle, ExpressionType returnType) {
        if (returnType != ScalarType.NUMBER) {
            return implementationHandle;
        }
        Class<?> sourceType = implementationHandle.type().returnType();
        if (sourceType == BigDecimal.class) {
            return implementationHandle;
        }
        return MethodHandles.filterReturnValue(implementationHandle, numericToBigDecimal(sourceType));
    }

    private static MethodType canonicalMethodType(List<ExpressionType> parameterTypes, ExpressionType returnType) {
        Class<?>[] canonicalParameterTypes = new Class<?>[parameterTypes.size()];
        for (int index = 0; index < parameterTypes.size(); index++) {
            canonicalParameterTypes[index] = ExpressionJavaTypes.valueType(parameterTypes.get(index));
        }
        return MethodType.methodType(ExpressionJavaTypes.valueType(returnType), canonicalParameterTypes);
    }

    private static MethodType canonicalInstanceMethodType(
            Class<?> receiverType,
            List<ExpressionType> parameterTypes,
            ExpressionType returnType) {
        Class<?>[] canonicalParameterTypes = new Class<?>[parameterTypes.size() + 1];
        canonicalParameterTypes[0] = receiverType;
        for (int index = 0; index < parameterTypes.size(); index++) {
            canonicalParameterTypes[index + 1] = ExpressionJavaTypes.valueType(parameterTypes.get(index));
        }
        return MethodType.methodType(ExpressionJavaTypes.valueType(returnType), canonicalParameterTypes);
    }

    private static MethodHandle bigDecimalToNumeric(Class<?> targetType) {
        NumericAdapter adapter = NUMERIC_ADAPTERS.get(targetType);
        if (adapter == null) {
            throw new IllegalArgumentException("unsupported numeric parameter type: " + targetType.getName());
        }
        return find(adapter.toNumericMethodName(), MethodType.methodType(adapter.javaType(), BigDecimal.class));
    }

    private static MethodHandle numericToBigDecimal(Class<?> sourceType) {
        NumericAdapter adapter = NUMERIC_ADAPTERS.get(sourceType);
        if (adapter == null) {
            throw new IllegalArgumentException("unsupported numeric return type: " + sourceType.getName());
        }
        return find(adapter.fromNumericMethodName(), MethodType.methodType(BigDecimal.class, adapter.javaType()));
    }

    private static Map.Entry<Class<?>, NumericAdapter> numeric(
            Class<?> javaType,
            String toNumericMethodName,
            String fromNumericMethodName) {
        return Map.entry(javaType, new NumericAdapter(javaType, toNumericMethodName, fromNumericMethodName));
    }

    private static MethodHandle find(String methodName, MethodType methodType) {
        try {
            return LOOKUP.findStatic(FunctionHandleAdapters.class, methodName, methodType);
        } catch (NoSuchMethodException | IllegalAccessException exception) {
            throw new IllegalStateException("failed to resolve function handle adapter " + methodName, exception);
        }
    }

    private static byte toByte(BigDecimal value) {
        return value.byteValueExact();
    }

    private static Byte toByteBox(BigDecimal value) {
        return value.byteValueExact();
    }

    private static short toShort(BigDecimal value) {
        return value.shortValueExact();
    }

    private static Short toShortBox(BigDecimal value) {
        return value.shortValueExact();
    }

    private static int toInt(BigDecimal value) {
        return value.intValueExact();
    }

    private static Integer toInteger(BigDecimal value) {
        return value.intValueExact();
    }

    private static long toLong(BigDecimal value) {
        return value.longValueExact();
    }

    private static Long toLongBox(BigDecimal value) {
        return value.longValueExact();
    }

    private static float toFloat(BigDecimal value) {
        return value.floatValue();
    }

    private static Float toFloatBox(BigDecimal value) {
        return value.floatValue();
    }

    private static double toDouble(BigDecimal value) {
        return value.doubleValue();
    }

    private static Double toDoubleBox(BigDecimal value) {
        return value.doubleValue();
    }

    private static BigDecimal fromByte(byte value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromByteBox(Byte value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromShort(short value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromShortBox(Short value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromInt(int value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromInteger(Integer value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromLong(long value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromLongBox(Long value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromFloat(float value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromFloatBox(Float value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromDouble(double value) {
        return BigDecimal.valueOf(value);
    }

    private static BigDecimal fromDoubleBox(Double value) {
        return BigDecimal.valueOf(value);
    }

    private static Object requireNonNull(Object value) {
        return Objects.requireNonNull(value, "function arguments and results must not be null");
    }

    private record NumericAdapter(
            Class<?> javaType,
            String toNumericMethodName,
            String fromNumericMethodName) {
    }
}
