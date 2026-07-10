package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static com.runestone.expeval_mk3.api.ScalarType.BOOLEAN;
import static com.runestone.expeval_mk3.api.ScalarType.DATE;
import static com.runestone.expeval_mk3.api.ScalarType.DATETIME;
import static com.runestone.expeval_mk3.api.ScalarType.NUMBER;
import static com.runestone.expeval_mk3.api.ScalarType.STRING;
import static com.runestone.expeval_mk3.api.ScalarType.TIME;

final class AssertionBuiltInFunctions {

    private final BoundaryCoercion boundaryCoercion;
    private final int materializationLimit;

    AssertionBuiltInFunctions(BoundaryCoercion boundaryCoercion, int materializationLimit) {
        this.boundaryCoercion = Objects.requireNonNull(boundaryCoercion, "boundaryCoercion");
        if (materializationLimit < 0) {
            throw new IllegalArgumentException("materializationLimit must not be negative");
        }
        this.materializationLimit = materializationLimit;
    }

    static List<FunctionDescriptor> descriptors(BoundaryCoercion boundaryCoercion, int materializationLimit) {
        FunctionPurity assertionPurity = boundaryCoercion.deterministicForConstants()
                ? FunctionPurity.FOLDABLE
                : FunctionPurity.PURE;
        AssertionBuiltInFunctions assertions = new AssertionBuiltInFunctions(boundaryCoercion, materializationLimit);
        List<FunctionDescriptor> descriptors = new java.util.ArrayList<>();
        descriptors.addAll(ReflectedFunctionImporter
                .importSelected(assertions, assertionPurity)
                .methods("asNumber", "asText", "asBool", "asDate", "asTime", "asDateTime")
                .toList());
        descriptors.addAll(ReflectedFunctionImporter
                .importSelected(assertions, FunctionPurity.FOLDABLE)
                .methods("asVector")
                .toList());
        return descriptors;
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

    public List<Object> asVector(Object value) {
        return BuiltInFunctionSupport.materializeVector(materializationLimit, value);
    }
}
