package com.runestone.expeval2.internal.runtime;

import com.runestone.converters.DataConversionService;
import com.runestone.expeval2.types.ResolvedType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/**
 * Unified runtime transformation facade that combines value wrapping and type coercion.
 *
 * <p>Holds a single {@link RuntimeValueFactory} and a single {@link RuntimeCoercionService},
 * both backed by the same {@link DataConversionService}. One instance is created per
 * {@link com.runestone.expeval2.environment.ExpressionEnvironment} and shared across all
 * evaluations performed within that environment.
 */
public final class RuntimeServices {

    private final RuntimeValueFactory valueFactory;
    private final RuntimeCoercionService coercionService;

    public RuntimeServices(DataConversionService conversionService) {
        Objects.requireNonNull(conversionService, "conversionService must not be null");
        this.valueFactory = new RuntimeValueFactory(conversionService);
        this.coercionService = new RuntimeCoercionService(conversionService);
    }

    // -------------------------------------------------------------------------
    // Value wrapping (RuntimeValueFactory)
    // -------------------------------------------------------------------------

    public RuntimeValue from(Object rawValue) {
        return valueFactory.from(rawValue);
    }

    public RuntimeValue from(Object rawValue, ResolvedType expectedType) {
        return valueFactory.from(rawValue, expectedType);
    }

    // -------------------------------------------------------------------------
    // Type coercion (RuntimeCoercionService)
    // -------------------------------------------------------------------------

    public Object coerce(RuntimeValue value, Class<?> targetType) {
        return coercionService.coerce(value, targetType);
    }

    public BigDecimal asNumber(RuntimeValue value) {
        return coercionService.asNumber(value);
    }

    public boolean asBoolean(RuntimeValue value) {
        return coercionService.asBoolean(value);
    }

    public String asString(RuntimeValue value) {
        return coercionService.asString(value);
    }

    public LocalDate asDate(RuntimeValue value) {
        return coercionService.asDate(value);
    }

    public LocalTime asTime(RuntimeValue value) {
        return coercionService.asTime(value);
    }

    public LocalDateTime asDateTime(RuntimeValue value) {
        return coercionService.asDateTime(value);
    }

    public double asDouble(RuntimeValue value) {
        return coercionService.asDouble(value);
    }

    public int asInt(RuntimeValue value) {
        return coercionService.asInt(value);
    }

    public long asLong(RuntimeValue value) {
        return coercionService.asLong(value);
    }

    public List<RuntimeValue> asVector(RuntimeValue value) {
        return coercionService.asVector(value);
    }
}
