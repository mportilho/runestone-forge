package com.runestone.expeval.internal.runtime;

import com.runestone.converters.DataConversionService;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class RuntimeCoercionService {

    private final DataConversionService conversionService;

    public RuntimeCoercionService(DataConversionService conversionService) {
        this.conversionService = Objects.requireNonNull(conversionService, "conversionService must not be null");
    }

    public BigDecimal asNumber(Object value) {
        if (value instanceof BigDecimal bd) return bd;
        if (value == null) throw new IllegalStateException("cannot coerce null to number");
        if (value instanceof Integer || value instanceof Long || value instanceof Short || value instanceof Byte) {
            long l = ((Number) value).longValue();
            if (l == 0) return BigDecimal.ZERO;
            if (l == 1) return BigDecimal.ONE;
            if (l == 10) return BigDecimal.TEN;
            return BigDecimal.valueOf(l);
        }
        if (value instanceof Double d) return BigDecimal.valueOf(d);
        if (value instanceof Float f) return BigDecimal.valueOf(f.doubleValue());
        if (value instanceof java.math.BigInteger bi) return new BigDecimal(bi);
        if (value instanceof Number n) return new BigDecimal(n.toString());
        return convert(value, BigDecimal.class);
    }

    public boolean asBoolean(Object value) {
        if (value instanceof Boolean b) return b;
        if (value == null) throw new IllegalStateException("cannot coerce null to boolean");
        return convert(value, Boolean.class);
    }

    public String asString(Object value) {
        if (value instanceof String s) return s;
        if (value == null) throw new IllegalStateException("cannot coerce null to string");
        return convert(value, String.class);
    }

    public LocalDate asDate(Object value) {
        if (value instanceof LocalDate d) return d;
        if (value == null) throw new IllegalStateException("cannot coerce null to date");
        return convert(value, LocalDate.class);
    }

    public LocalTime asTime(Object value) {
        if (value instanceof LocalTime t) return t;
        if (value == null) throw new IllegalStateException("cannot coerce null to time");
        return convert(value, LocalTime.class);
    }

    public LocalDateTime asDateTime(Object value) {
        if (value instanceof LocalDateTime dt) return dt;
        if (value == null) throw new IllegalStateException("cannot coerce null to datetime");
        return convert(value, LocalDateTime.class);
    }

    public double asDouble(Object value) {
        return asNumber(value).doubleValue();
    }

    public int asInt(Object value) {
        return asNumber(value).intValue();
    }

    public long asLong(Object value) {
        return asNumber(value).longValue();
    }

    public Object coerce(Object value, Class<?> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (value == null) return null;
        if (targetType == Object.class || targetType.isInstance(value)) return value;
        if (targetType == BigDecimal.class) return asNumber(value);
        if (targetType == Double.class || targetType == double.class) return asDouble(value);
        if (targetType == Integer.class || targetType == int.class) return asInt(value);
        if (targetType == Long.class || targetType == long.class) return asLong(value);
        if (targetType == Boolean.class || targetType == boolean.class) return asBoolean(value);
        if (targetType == String.class) return asString(value);
        if (targetType == LocalDate.class) return asDate(value);
        if (targetType == LocalTime.class) return asTime(value);
        if (targetType == LocalDateTime.class) return asDateTime(value);
        if (value instanceof List<?> elements) {
            if (List.class.isAssignableFrom(targetType)) {
                return new ArrayList<>(elements);
            }
            if (!targetType.isArray()) {
                return convert(value, targetType);
            }
            int n = elements.size();
            Class<?> componentType = targetType.getComponentType();
            if (componentType == BigDecimal.class) {
                BigDecimal[] array = new BigDecimal[n];
                for (int i = 0; i < n; i++) array[i] = asNumber(elements.get(i));
                return array;
            }
            if (componentType == double.class) {
                double[] array = new double[n];
                for (int i = 0; i < n; i++) array[i] = asDouble(elements.get(i));
                return array;
            }
            if (componentType == int.class) {
                int[] array = new int[n];
                for (int i = 0; i < n; i++) array[i] = asInt(elements.get(i));
                return array;
            }
            if (componentType == long.class) {
                long[] array = new long[n];
                for (int i = 0; i < n; i++) array[i] = asLong(elements.get(i));
                return array;
            }
            if (!componentType.isPrimitive()) {
                Object[] array = (Object[]) Array.newInstance(componentType, n);
                for (int i = 0; i < n; i++) array[i] = coerce(elements.get(i), componentType);
                return array;
            }
            Object array = Array.newInstance(componentType, n);
            for (int i = 0; i < n; i++) Array.set(array, i, coerce(elements.get(i), componentType));
            return array;
        }
        return convert(value, targetType);
    }

    private <T> T convert(Object rawValue, Class<T> targetType) {
        T converted = conversionService.convert(rawValue, targetType);
        if (converted == null) {
            throw new IllegalArgumentException("cannot convert value '" + rawValue + "' to " + targetType.getSimpleName());
        }
        return converted;
    }
}
