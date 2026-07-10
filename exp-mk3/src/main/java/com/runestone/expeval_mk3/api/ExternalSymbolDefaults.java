package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class ExternalSymbolDefaults {

    private ExternalSymbolDefaults() {
    }

    static ExpressionType inferType(String name, Object value) {
        return switch (value) {
            case null -> NullType.INSTANCE;
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
            default -> throw new IllegalArgumentException("unsupported default value type for external symbol '"
                    + name
                    + "': "
                    + value.getClass().getName());
        };
    }

    static Object canonicalize(String name, ExpressionType type, Object value) {
        return switch (type) {
            case ScalarType scalarType -> canonicalizeScalarDefault(name, scalarType, value);
            case VectorType vectorType -> canonicalizeCollectionDefault(
                    name,
                    vectorType.elementType(),
                    value,
                    "VectorType");
            case CollectionType collectionType -> canonicalizeCollectionDefault(
                    name,
                    collectionType.elementType(),
                    value,
                    "CollectionType");
            case MapType mapType -> canonicalizeMapDefault(name, mapType.valueType(), value);
            case ObjectType ignored -> throw new IllegalArgumentException(
                    "object defaults are not supported until Java type metadata is available for external symbol '"
                            + name
                            + "'");
            case NullType ignored -> {
                if (value != null) {
                    throw new IllegalArgumentException("external symbol '" + name + "' default must be null");
                }
                yield null;
            }
            case UnknownType ignored -> canonicalize(name, inferType(name, value), value);
        };
    }

    static String canonicalValue(Object value) {
        StringBuilder canonical = new StringBuilder(64);
        appendCanonicalDefaultValue(canonical, value);
        return canonical.toString();
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
                elementType = UnknownType.INSTANCE;
            }
        }
        return elementType == null ? UnknownType.INSTANCE : elementType;
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
                valueType = UnknownType.INSTANCE;
            }
        }
        return valueType == null ? UnknownType.INSTANCE : valueType;
    }

    private static Object canonicalizeScalarDefault(String name, ScalarType type, Object value) {
        return switch (type) {
            case NUMBER -> canonicalizeNumericDefault(name, value);
            case BOOLEAN -> {
                if (!(value instanceof Boolean)) {
                    throw defaultTypeMismatch(name, type, value);
                }
                yield value;
            }
            case STRING -> {
                if (!(value instanceof String)) {
                    throw defaultTypeMismatch(name, type, value);
                }
                yield value;
            }
            case DATE -> {
                if (!(value instanceof LocalDate)) {
                    throw defaultTypeMismatch(name, type, value);
                }
                yield value;
            }
            case TIME -> {
                if (!(value instanceof LocalTime)) {
                    throw defaultTypeMismatch(name, type, value);
                }
                yield value;
            }
            case DATETIME -> {
                if (!(value instanceof LocalDateTime)) {
                    throw defaultTypeMismatch(name, type, value);
                }
                yield value;
            }
        };
    }

    private static BigDecimal canonicalizeNumericDefault(String name, Object value) {
        return switch (value) {
            case BigDecimal number -> number;
            case BigInteger number -> new BigDecimal(number);
            case Byte number -> BigDecimal.valueOf(number.longValue());
            case Short number -> BigDecimal.valueOf(number.longValue());
            case Integer number -> BigDecimal.valueOf(number.longValue());
            case Long number -> BigDecimal.valueOf(number);
            case Float number -> {
                finiteFloatingPointType(name, number);
                yield BigDecimal.valueOf(number.doubleValue());
            }
            case Double number -> {
                finiteFloatingPointType(name, number);
                yield BigDecimal.valueOf(number);
            }
            default -> throw defaultTypeMismatch(name, ScalarType.NUMBER, value);
        };
    }

    private static List<Object> canonicalizeCollectionDefault(
            String name,
            ExpressionType elementType,
            Object value,
            String typeName) {
        if (!(value instanceof Collection<?> values)) {
            throw new IllegalArgumentException(
                    "external symbol '" + name + "' default must be a collection for " + typeName);
        }
        List<Object> canonicalValues = new ArrayList<>(values.size());
        for (Object element : values) {
            canonicalValues.add(canonicalizeElementDefault(name, elementType, element));
        }
        return Collections.unmodifiableList(canonicalValues);
    }

    private static Object canonicalizeElementDefault(String name, ExpressionType elementType, Object value) {
        if (elementType == UnknownType.INSTANCE) {
            return canonicalize(name, inferType(name, value), value);
        }
        return canonicalize(name, elementType, value);
    }

    private static Map<String, Object> canonicalizeMapDefault(String name, ExpressionType valueType, Object value) {
        if (!(value instanceof Map<?, ?> values)) {
            throw new IllegalArgumentException("external symbol '" + name + "' default must be a map for MapType");
        }
        TreeMap<String, Object> sortedValues = new TreeMap<>();
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException(
                        "MapType defaults must be text-keyed for external symbol '" + name + "'");
            }
            sortedValues.put(key, canonicalizeElementDefault(name, valueType, entry.getValue()));
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(sortedValues));
    }

    private static void appendCanonicalDefaultValue(StringBuilder canonical, Object value) {
        switch (value) {
            case null -> canonical.append("null");
            case BigDecimal number -> appendCanonicalValue(canonical, "number", number.toPlainString());
            case Boolean bool -> appendCanonicalValue(canonical, "boolean", Boolean.toString(bool));
            case String text -> appendCanonicalValue(canonical, "string", text);
            case LocalDate date -> appendCanonicalValue(canonical, "date", date.toString());
            case LocalTime time -> appendCanonicalValue(canonical, "time", time.toString());
            case LocalDateTime dateTime -> appendCanonicalValue(canonical, "datetime", dateTime.toString());
            case List<?> list -> appendCanonicalList(canonical, list);
            case Map<?, ?> map -> appendCanonicalMap(canonical, map);
            default -> throw new IllegalArgumentException(
                    "unsupported external symbol default value type: " + value.getClass().getName());
        }
    }

    private static void appendCanonicalValue(StringBuilder canonical, String type, String value) {
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        canonical.append(type).append(':').append(valueBytes.length).append(':').append(value);
    }

    private static void appendCanonicalList(StringBuilder canonical, List<?> values) {
        canonical.append("list:").append(values.size()).append('[');
        for (Object value : values) {
            String item = canonicalValue(value);
            canonical.append(item.length()).append(':').append(item).append(';');
        }
        canonical.append(']');
    }

    private static void appendCanonicalMap(StringBuilder canonical, Map<?, ?> values) {
        canonical.append("map:").append(values.size()).append('{');
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            String key = (String) entry.getKey();
            String value = canonicalValue(entry.getValue());
            canonical.append(key.length())
                    .append(':')
                    .append(key)
                    .append('=')
                    .append(value.length())
                    .append(':')
                    .append(value)
                    .append(';');
        }
        canonical.append('}');
    }

    private static IllegalArgumentException defaultTypeMismatch(String name, ExpressionType expectedType, Object value) {
        String actualType = value == null ? "null" : value.getClass().getName();
        return new IllegalArgumentException(
                "external symbol '" + name + "' default is not compatible with " + expectedType + ": " + actualType);
    }
}
