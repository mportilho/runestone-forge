package com.runestone.expeval_mk3.api;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.runestone.expeval_mk3.api.ScalarType.BOOLEAN;
import static com.runestone.expeval_mk3.api.ScalarType.DATE;
import static com.runestone.expeval_mk3.api.ScalarType.DATETIME;
import static com.runestone.expeval_mk3.api.ScalarType.NUMBER;
import static com.runestone.expeval_mk3.api.ScalarType.STRING;
import static com.runestone.expeval_mk3.api.ScalarType.TIME;

final class AssertionBuiltInFunctions {

    private static final List<ScalarType> ASSERTION_SOURCE_TYPES = List.of(
            NUMBER,
            BOOLEAN,
            STRING,
            DATE,
            TIME,
            DATETIME);
    private static final int ASSERTION_FUNCTION_COUNT = 6;
    private static final int ASSERTION_DESCRIPTOR_COUNT = ASSERTION_FUNCTION_COUNT * ASSERTION_SOURCE_TYPES.size();

    private final BoundaryCoercion boundaryCoercion;

    AssertionBuiltInFunctions(BoundaryCoercion boundaryCoercion) {
        this.boundaryCoercion = Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
    }

    static List<FunctionDescriptor> descriptors(BoundaryCoercion boundaryCoercion) {
        FunctionPurity assertionPurity = boundaryCoercion.deterministicForConstants()
                ? FunctionPurity.FOLDABLE
                : FunctionPurity.PURE;
        AssertionBuiltInFunctions assertions = new AssertionBuiltInFunctions(boundaryCoercion);
        List<FunctionDescriptor> descriptors = new ArrayList<>(ASSERTION_DESCRIPTOR_COUNT);
        addDescriptors(descriptors, assertions, "asNumber", NUMBER, assertionPurity);
        addDescriptors(descriptors, assertions, "asText", STRING, assertionPurity);
        addDescriptors(descriptors, assertions, "asBool", BOOLEAN, assertionPurity);
        addDescriptors(descriptors, assertions, "asDate", DATE, assertionPurity);
        addDescriptors(descriptors, assertions, "asTime", TIME, assertionPurity);
        addDescriptors(descriptors, assertions, "asDateTime", DATETIME, assertionPurity);
        return List.copyOf(descriptors);
    }

    private static void addDescriptors(
            List<FunctionDescriptor> descriptors,
            AssertionBuiltInFunctions assertions,
            String methodName,
            ExpressionType returnType,
            FunctionPurity purity) {
        Method method = assertionMethod(methodName);
        MethodHandle handle = assertionHandle(assertions, method);
        for (ScalarType sourceType : ASSERTION_SOURCE_TYPES) {
            descriptors.add(descriptor(methodName, method, handle, sourceType, returnType, purity));
        }
    }

    public BigDecimal asNumber(Object value) {
        return (BigDecimal) boundaryCoercion.convertFunctionBindingFallback(value, NUMBER);
    }

    public String asText(Object value) {
        return (String) boundaryCoercion.convertFunctionBindingFallback(value, STRING);
    }

    public Boolean asBool(Object value) {
        return (Boolean) boundaryCoercion.convertFunctionBindingFallback(value, BOOLEAN);
    }

    public LocalDate asDate(Object value) {
        return (LocalDate) boundaryCoercion.convertFunctionBindingFallback(value, DATE);
    }

    public LocalTime asTime(Object value) {
        return (LocalTime) boundaryCoercion.convertFunctionBindingFallback(value, TIME);
    }

    public LocalDateTime asDateTime(Object value) {
        return (LocalDateTime) boundaryCoercion.convertFunctionBindingFallback(value, DATETIME);
    }

    private static FunctionDescriptor descriptor(
            String methodName,
            Method method,
            MethodHandle handle,
            ExpressionType parameterType,
            ExpressionType returnType,
            FunctionPurity purity) {
        return FunctionDescriptor.fromHandle(
                methodName,
                handle,
                FunctionImplementationMetadata.forMethod(method),
                List.of(parameterType),
                returnType,
                purity);
    }

    private static Method assertionMethod(String methodName) {
        try {
            return AssertionBuiltInFunctions.class.getDeclaredMethod(methodName, Object.class);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("failed to register assertion function: " + methodName, exception);
        }
    }

    private static MethodHandle assertionHandle(AssertionBuiltInFunctions assertions, Method method) {
        try {
            return MethodHandles.lookup().unreflect(method).bindTo(assertions);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("failed to access assertion function: " + method.getName(), exception);
        }
    }
}
