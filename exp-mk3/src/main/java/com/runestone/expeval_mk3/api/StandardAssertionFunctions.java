package com.runestone.expeval_mk3.api;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

final class StandardAssertionFunctions {

    private StandardAssertionFunctions() {
    }

    public static BigDecimal asNumber(BoundaryCoercion boundaryCoercion, Object value) {
        return (BigDecimal) boundaryCoercion.convertFunctionBindingFallback(value, ScalarType.NUMBER);
    }

    public static String asText(BoundaryCoercion boundaryCoercion, Object value) {
        return (String) boundaryCoercion.convertFunctionBindingFallback(value, ScalarType.STRING);
    }

    public static Boolean asBool(BoundaryCoercion boundaryCoercion, Object value) {
        return (Boolean) boundaryCoercion.convertFunctionBindingFallback(value, ScalarType.BOOLEAN);
    }

    public static LocalDate asDate(BoundaryCoercion boundaryCoercion, Object value) {
        return (LocalDate) boundaryCoercion.convertFunctionBindingFallback(value, ScalarType.DATE);
    }

    public static LocalTime asTime(BoundaryCoercion boundaryCoercion, Object value) {
        return (LocalTime) boundaryCoercion.convertFunctionBindingFallback(value, ScalarType.TIME);
    }

    public static LocalDateTime asDateTime(BoundaryCoercion boundaryCoercion, Object value) {
        return (LocalDateTime) boundaryCoercion.convertFunctionBindingFallback(value, ScalarType.DATETIME);
    }

    public static List<Object> asVector(BoundaryCoercion ignored, int materializationLimit, Object value) {
        if (value instanceof Map<?, ?>) {
            throw new IllegalArgumentException("asVector rejects map values as ambiguous");
        }
        if (value instanceof Collection<?> values) {
            enforceMaterializationLimit(materializationLimit, values.size());
            return List.copyOf(values);
        }
        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            enforceMaterializationLimit(materializationLimit, length);
            ArrayList<Object> result = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                result.add(Array.get(value, index));
            }
            return List.copyOf(result);
        }
        throw new IllegalArgumentException("asVector requires a vector, collection, or array value");
    }

    private static void enforceMaterializationLimit(int materializationLimit, int size) {
        if (size > materializationLimit) {
            throw new IllegalArgumentException(
                    "asVector materialization limit exceeded: " + size + " > " + materializationLimit);
        }
    }
}
