package com.runestone.expeval.internal.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;
import com.runestone.expeval.types.VectorType;

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
 * <p>One instance is created per {@link com.runestone.expeval.environment.ExpressionEnvironment}
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

    /**
     * Coerces {@code value} to {@code targetType} with a fast-path for already-matching types.
     *
     * <p>Returns {@code value} immediately when {@code targetType.isInstance(value)},
     * avoiding any intermediate allocation. Falls back to the coercion service only on type mismatch.
     */
    public Object coerce(Object value, Class<?> targetType) {
        Objects.requireNonNull(targetType, "targetType must not be null");
        if (value == null) return null;
        if (targetType == Object.class || targetType.isInstance(value)) return value;
        return coercionService.coerce(value, targetType);
    }

    /**
     * Normalizes {@code value} to the Java type corresponding to {@code resolvedType}.
     * Returns {@code value} unchanged when the resolved type is unknown, null, or non-scalar.
     */
    Object coerceToResolvedType(Object value, ResolvedType resolvedType) {
        if (resolvedType == null || resolvedType == UnknownType.INSTANCE) return value;
        if (value == null) return null;
        if (resolvedType == VectorType.INSTANCE) {
            if (value instanceof List<?>) return value;
            if (value.getClass().isArray()) {
                int len = Array.getLength(value);
                List<Object> list = new ArrayList<>(len);
                for (int i = 0; i < len; i++) list.add(Array.get(value, i));
                return list;
            }
            return value;
        }
        if (!(resolvedType instanceof ScalarType scalarType)) return value;
        return switch (scalarType) {
            case NUMBER   -> coerce(value, BigDecimal.class);
            case BOOLEAN  -> coerce(value, Boolean.class);
            case STRING   -> coerce(value, String.class);
            case DATE     -> coerce(value, LocalDate.class);
            case TIME     -> coerce(value, LocalTime.class);
            case DATETIME -> coerce(value, LocalDateTime.class);
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
