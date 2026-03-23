package com.runestone.expeval2.internal.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval2.types.ResolvedType;
import com.runestone.expeval2.types.ScalarType;
import com.runestone.expeval2.types.UnknownType;
import com.runestone.expeval2.types.VectorType;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Unified runtime type-coercion facade.
 *
 * <p>One instance is created per {@link com.runestone.expeval2.environment.ExpressionEnvironment}
 * and shared across all evaluations performed within that environment.
 */
public final class RuntimeServices {

    private final RuntimeCoercionService coercionService;

    public RuntimeServices(DataConversionService conversionService) {
        Objects.requireNonNull(conversionService, "conversionService must not be null");
        this.coercionService = new RuntimeCoercionService(conversionService);
    }

    // -------------------------------------------------------------------------
    // General coercion
    // -------------------------------------------------------------------------

    public Object coerce(Object value, Class<?> targetType) {
        return coercionService.coerce(value, targetType);
    }

    /**
     * Coerces {@code rawValue} to {@code targetType} with a fast-path for already-matching types.
     *
     * <p>Returns {@code rawValue} immediately when {@code targetType.isInstance(rawValue)},
     * avoiding any intermediate allocation. Falls back to {@link #coerce} only on type mismatch.
     */
    public Object coerceRaw(Object rawValue, Class<?> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (rawValue == null) return null;
        if (targetType == Object.class || targetType.isInstance(rawValue)) return rawValue;
        return coercionService.coerce(rawValue, targetType);
    }

    /**
     * Normalizes {@code rawValue} to the Java type corresponding to {@code resolvedType}.
     * Returns {@code rawValue} unchanged when the resolved type is unknown, null, or non-scalar.
     */
    Object coerceToResolvedType(Object rawValue, ResolvedType resolvedType) {
        if (resolvedType == null || resolvedType == UnknownType.INSTANCE) return rawValue;
        if (rawValue == null) return null;
        if (resolvedType == VectorType.INSTANCE) {
            if (rawValue instanceof List<?>) return rawValue;
            if (rawValue.getClass().isArray()) {
                int len = Array.getLength(rawValue);
                List<Object> list = new ArrayList<>(len);
                for (int i = 0; i < len; i++) list.add(Array.get(rawValue, i));
                return list;
            }
            return rawValue;
        }
        if (!(resolvedType instanceof ScalarType scalarType)) return rawValue;
        return switch (scalarType) {
            case NUMBER   -> coerceRaw(rawValue, BigDecimal.class);
            case BOOLEAN  -> coerceRaw(rawValue, Boolean.class);
            case STRING   -> coerceRaw(rawValue, String.class);
            case DATE     -> coerceRaw(rawValue, LocalDate.class);
            case TIME     -> coerceRaw(rawValue, LocalTime.class);
            case DATETIME -> coerceRaw(rawValue, LocalDateTime.class);
        };
    }

    // -------------------------------------------------------------------------
    // Type-specific extractors
    // -------------------------------------------------------------------------

    public BigDecimal asNumber(Object value) {
        return coercionService.asNumber(value);
    }

    public boolean asBoolean(Object value) {
        return coercionService.asBoolean(value);
    }

    public String asString(Object value) {
        return coercionService.asString(value);
    }

    public LocalDate asDate(Object value) {
        return coercionService.asDate(value);
    }

    public LocalTime asTime(Object value) {
        return coercionService.asTime(value);
    }

    public LocalDateTime asDateTime(Object value) {
        return coercionService.asDateTime(value);
    }

    public double asDouble(Object value) {
        return coercionService.asDouble(value);
    }

    public int asInt(Object value) {
        return coercionService.asInt(value);
    }

    public long asLong(Object value) {
        return coercionService.asLong(value);
    }
}
