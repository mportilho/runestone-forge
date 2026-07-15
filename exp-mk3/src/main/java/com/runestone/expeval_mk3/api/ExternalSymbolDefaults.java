package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;

final class ExternalSymbolDefaults {

    private ExternalSymbolDefaults() {
    }

    static ExpressionType inferType(String name, Object value) {
        return switch (value) {
            case null -> throw new IllegalArgumentException("external symbol '" + name + "' default must not be null");
            case BigDecimal ignored -> ScalarType.NUMBER;
            case BigInteger ignored -> ScalarType.NUMBER;
            case Byte ignored -> ScalarType.NUMBER;
            case Short ignored -> ScalarType.NUMBER;
            case Integer ignored -> ScalarType.NUMBER;
            case Long ignored -> ScalarType.NUMBER;
            case Float number -> finiteFloatingPointType(name, number);
            case Double number -> finiteFloatingPointType(name, number);
            case Boolean ignored -> ScalarType.BOOLEAN;
            case String ignored -> ScalarType.STRING;
            case LocalDate ignored -> ScalarType.DATE;
            case LocalTime ignored -> ScalarType.TIME;
            case LocalDateTime ignored -> ScalarType.DATETIME;
            case Collection<?> values -> new CollectionType(inferElementType(name, values));
            case Map<?, ?> values -> new MapType(inferMapValueType(name, values));
            default -> new ObjectType(value.getClass().getName());
        };
    }

    static Object canonicalize(String name, ExpressionType type, Object value) {
        return canonicalize(name, type, value, BoundaryCoercion.standard());
    }

    static Object canonicalize(String name, ExpressionType type, Object value, BoundaryCoercion boundaryCoercion) {
        return boundaryCoercion.convertDefault(name, value, type);
    }

    private static ScalarType finiteFloatingPointType(String name, Float number) {
        if (!Float.isFinite(number)) {
            throw new IllegalArgumentException("external symbol '" + name + "' has a non-finite numeric default");
        }
        return ScalarType.NUMBER;
    }

    private static ScalarType finiteFloatingPointType(String name, Double number) {
        if (!Double.isFinite(number)) {
            throw new IllegalArgumentException("external symbol '" + name + "' has a non-finite numeric default");
        }
        return ScalarType.NUMBER;
    }

    private static ExpressionType inferElementType(String name, Collection<?> values) {
        ExpressionType elementType = null;
        for (Object value : values) {
            ExpressionType currentType = inferType(name, value);
            if (elementType == null) {
                elementType = currentType;
            } else if (!elementType.equals(currentType)) {
                throw new IllegalArgumentException(
                        "external symbol '" + name + "' has a heterogeneous collection default");
            }
        }
        if (elementType == null) {
            throw new IllegalArgumentException(
                    "external symbol '" + name + "' cannot infer a type from an empty collection default");
        }
        return elementType;
    }

    private static ExpressionType inferMapValueType(String name, Map<?, ?> values) {
        ExpressionType valueType = null;
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                throw new IllegalArgumentException(
                        "MapType defaults must be text-keyed for external symbol '" + name + "'");
            }
            ExpressionType currentType = inferType(name, entry.getValue());
            if (valueType == null) {
                valueType = currentType;
            } else if (!valueType.equals(currentType)) {
                throw new IllegalArgumentException("external symbol '" + name + "' has a heterogeneous map default");
            }
        }
        if (valueType == null) {
            throw new IllegalArgumentException(
                    "external symbol '" + name + "' cannot infer a type from an empty map default");
        }
        return valueType;
    }

}
