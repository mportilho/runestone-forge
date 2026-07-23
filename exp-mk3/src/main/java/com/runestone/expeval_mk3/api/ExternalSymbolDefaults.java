package com.runestone.expeval_mk3.api;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

final class ExternalSymbolDefaults {

    private ExternalSymbolDefaults() {
    }

    static PreparedDefault prepare(
            String name,
            Object value,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        if (value == null) {
            throw new IllegalArgumentException("external symbol '" + name + "' default must not be null");
        }
        if (value.getClass().isArray()) {
            return prepareArray(name, value, boundaryCoercion, maxMaterializedSize);
        }
        if (value instanceof Map<?, ?> values) {
            return prepareMap(name, values, boundaryCoercion, maxMaterializedSize);
        }
        if (value instanceof Iterable<?> values) {
            return prepareIterable(name, values, boundaryCoercion, maxMaterializedSize);
        }
        ExpressionType type = inferScalarOrObjectType(name, value);
        Object canonicalValue = boundaryCoercion.convertDefault(name, value, type, maxMaterializedSize);
        return new PreparedDefault(type, canonicalValue);
    }

    private static PreparedDefault prepareArray(
            String name,
            Object values,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        int length = Array.getLength(values);
        requireWithinLimit(name, length, maxMaterializedSize);
        ArrayList<Object> snapshot = new ArrayList<>(length);
        ExpressionType elementType = null;
        for (int index = 0; index < length; index++) {
            PreparedDefault element = prepare(name, Array.get(values, index), boundaryCoercion, maxMaterializedSize);
            elementType = commonElementType(name, elementType, element.type(), "collection");
            snapshot.add(element.value());
        }
        if (elementType == null) {
            elementType = inferClassType(values.getClass().getComponentType());
        }
        return new PreparedDefault(
                new CollectionType(elementType),
                Collections.unmodifiableList(snapshot));
    }

    private static PreparedDefault prepareIterable(
            String name,
            Iterable<?> values,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        if (values instanceof Collection<?> collection) {
            requireWithinLimit(name, collection.size(), maxMaterializedSize);
        }
        ArrayList<Object> snapshot = new ArrayList<>(initialCapacity(values, maxMaterializedSize));
        ExpressionType elementType = null;
        for (Object value : values) {
            if (snapshot.size() == maxMaterializedSize) {
                throw materializationLimitExceeded(name, maxMaterializedSize);
            }
            PreparedDefault element = prepare(name, value, boundaryCoercion, maxMaterializedSize);
            elementType = commonElementType(name, elementType, element.type(), "collection");
            snapshot.add(element.value());
        }
        if (elementType == null) {
            throw new IllegalArgumentException(
                    "external symbol '" + name + "' cannot infer a type from an empty collection default");
        }
        return new PreparedDefault(
                new CollectionType(elementType),
                Collections.unmodifiableList(snapshot));
    }

    private static PreparedDefault prepareMap(
            String name,
            Map<?, ?> values,
            BoundaryCoercion boundaryCoercion,
            int maxMaterializedSize) {
        requireWithinLimit(name, values.size(), maxMaterializedSize);
        TreeMap<String, Object> snapshot = new TreeMap<>();
        ExpressionType valueType = null;
        int entryCount = 0;
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (entryCount == maxMaterializedSize) {
                throw materializationLimitExceeded(name, maxMaterializedSize);
            }
            entryCount++;
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException(
                        "MapType defaults must be text-keyed for external symbol '" + name + "'");
            }
            PreparedDefault value = prepare(name, entry.getValue(), boundaryCoercion, maxMaterializedSize);
            valueType = commonElementType(name, valueType, value.type(), "map");
            snapshot.put(key, value.value());
        }
        if (valueType == null) {
            throw new IllegalArgumentException(
                    "external symbol '" + name + "' cannot infer a type from an empty map default");
        }
        return new PreparedDefault(
                new MapType(valueType),
                Collections.unmodifiableMap(new LinkedHashMap<>(snapshot)));
    }

    private static ExpressionType inferScalarOrObjectType(String name, Object value) {
        return switch (value) {
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
            default -> new ObjectType(value.getClass().getName());
        };
    }

    private static ExpressionType inferClassType(Class<?> sourceType) {
        if (sourceType.isArray()) {
            return new CollectionType(inferClassType(sourceType.getComponentType()));
        }
        Class<?> boxedType = ExpressionJavaTypes.boxed(sourceType);
        if (Number.class.isAssignableFrom(boxedType)) {
            return ScalarType.NUMBER;
        }
        if (boxedType == Boolean.class) {
            return ScalarType.BOOLEAN;
        }
        if (boxedType == String.class) {
            return ScalarType.STRING;
        }
        if (boxedType == LocalDate.class) {
            return ScalarType.DATE;
        }
        if (boxedType == LocalTime.class) {
            return ScalarType.TIME;
        }
        if (boxedType == LocalDateTime.class) {
            return ScalarType.DATETIME;
        }
        return new ObjectType(boxedType.getName());
    }

    private static ExpressionType commonElementType(
            String name,
            ExpressionType currentType,
            ExpressionType nextType,
            String containerName) {
        if (currentType == null) {
            return nextType;
        }
        if (!currentType.equals(nextType)) {
            throw new IllegalArgumentException(
                    "external symbol '" + name + "' has a heterogeneous " + containerName + " default");
        }
        return currentType;
    }

    private static int initialCapacity(Iterable<?> values, int maxMaterializedSize) {
        if (values instanceof Collection<?> collection) {
            return collection.size();
        }
        return Math.min(maxMaterializedSize, 10);
    }

    private static void requireWithinLimit(String name, int size, int maxMaterializedSize) {
        if (size > maxMaterializedSize) {
            throw materializationLimitExceeded(name, maxMaterializedSize);
        }
    }

    private static IllegalArgumentException materializationLimitExceeded(String name, int maxMaterializedSize) {
        return new IllegalArgumentException(
                "external symbol '" + name + "' default exceeds maxMaterializedSize " + maxMaterializedSize);
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

    record PreparedDefault(ExpressionType type, Object value) {
    }
}
