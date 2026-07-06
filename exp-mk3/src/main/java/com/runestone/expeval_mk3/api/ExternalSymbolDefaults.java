package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        return canonicalize(name, type, value, BoundaryCoercion.standard());
    }

    static Object canonicalize(String name, ExpressionType type, Object value, BoundaryCoercion boundaryCoercion) {
        return boundaryCoercion.convertDefault(name, value, type);
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

}
