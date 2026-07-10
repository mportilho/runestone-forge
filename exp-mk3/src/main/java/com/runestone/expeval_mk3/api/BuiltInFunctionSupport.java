package com.runestone.expeval_mk3.api;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.math.BigDecimal.ZERO;

final class BuiltInFunctionSupport {

    private BuiltInFunctionSupport() {
    }

    static BigDecimal[] numbers(List<?> values) {
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

    static <T> List<T> cast(List<?> values, Class<T> valueType) {
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

    static <T extends Comparable<? super T>> T max(List<T> values) {
        T result = values.getFirst();
        for (int index = 1; index < values.size(); index++) {
            T value = values.get(index);
            if (result.compareTo(value) < 0) {
                result = value;
            }
        }
        return result;
    }

    static <T extends Comparable<? super T>> T min(List<T> values) {
        T result = values.getFirst();
        for (int index = 1; index < values.size(); index++) {
            T value = values.get(index);
            if (result.compareTo(value) > 0) {
                result = value;
            }
        }
        return result;
    }

    static int integer(BigDecimal value) {
        return value.intValueExact();
    }

    static long longValue(BigDecimal value) {
        return value.longValueExact();
    }

    static String requireText(String value) {
        return requireString(value, "value");
    }

    static String requireString(Object value, String name) {
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException(name + " must be text");
        }
        return text;
    }

    static String requirePadding(String padding) {
        String value = requireString(padding, "padding");
        if (value.isEmpty()) {
            throw new IllegalArgumentException("padding must not be empty");
        }
        return value;
    }

    static String pad(String value, int size, String padding, boolean leftAlignedPadding) {
        if (size <= value.length()) {
            return value;
        }
        String filler = repeatToLength(padding, size - value.length());
        return leftAlignedPadding ? filler + value : value + filler;
    }

    static List<Object> materializeVector(int materializationLimit, Object value) {
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

    static double kahanSum(BigDecimal[] values) {
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

    static double kahanSumAbsoluteDeviations(BigDecimal[] values, double mean) {
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

    static BigDecimal zeroAtScale(BigDecimal value) {
        return ZERO.setScale(value.scale(), java.math.RoundingMode.HALF_EVEN);
    }
}
