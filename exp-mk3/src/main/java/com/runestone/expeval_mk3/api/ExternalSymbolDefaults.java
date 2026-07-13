package com.runestone.expeval_mk3.api;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    static String canonicalValue(String name, ExpressionType type, Object value) {
        StringBuilder canonical = new StringBuilder(64);
        appendCanonicalDefaultValue(canonical, name, type, value);
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

    private static void appendCanonicalDefaultValue(
            StringBuilder canonical,
            String name,
            ExpressionType type,
            Object value) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(value, "value");
        switch (type) {
            case ScalarType.NUMBER -> appendCanonicalValue(canonical, "number", ((BigDecimal) value).toPlainString());
            case ScalarType.BOOLEAN -> appendCanonicalValue(canonical, "boolean", Boolean.toString((Boolean) value));
            case ScalarType.STRING -> appendCanonicalValue(canonical, "string", (String) value);
            case ScalarType.DATE -> appendCanonicalValue(canonical, "date", value.toString());
            case ScalarType.TIME -> appendCanonicalValue(canonical, "time", value.toString());
            case ScalarType.DATETIME -> appendCanonicalValue(canonical, "datetime", value.toString());
            case VectorType vectorType -> appendCanonicalList(canonical, name, (List<?>) value, vectorType.elementType());
            case CollectionType collectionType -> appendCanonicalList(
                    canonical,
                    name,
                    (List<?>) value,
                    collectionType.elementType());
            case MapType mapType -> appendCanonicalMap(canonical, name, (Map<?, ?>) value, mapType.valueType());
            case ObjectType objectType -> appendCanonicalObject(canonical, name, objectType, value);
        }
    }

    private static void appendCanonicalValue(StringBuilder canonical, String type, String value) {
        byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
        canonical.append(type).append(':').append(valueBytes.length).append(':').append(value);
    }

    private static void appendCanonicalList(
            StringBuilder canonical,
            String name,
            List<?> values,
            ExpressionType elementType) {
        canonical.append("list:").append(values.size()).append('[');
        for (int index = 0; index < values.size(); index++) {
            String item = canonicalValue(name + '[' + index + ']', elementType, values.get(index));
            canonical.append(item.length()).append(':').append(item).append(';');
        }
        canonical.append(']');
    }

    private static void appendCanonicalMap(
            StringBuilder canonical,
            String name,
            Map<?, ?> values,
            ExpressionType valueType) {
        canonical.append("map:").append(values.size()).append('{');
        for (Map.Entry<?, ?> entry : values.entrySet()) {
            String key = (String) entry.getKey();
            String value = canonicalValue(name + '[' + key + ']', valueType, entry.getValue());
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

    private static void appendCanonicalObject(
            StringBuilder canonical,
            String name,
            ObjectType objectType,
            Object value) {
        Class<?> valueType = value.getClass();
        List<ObjectComponent> components = objectComponents(valueType, value);
        if (components.isEmpty()) {
            throw new IllegalArgumentException(
                    "ObjectType defaults require record components or bean properties for canonical identity: "
                            + name
                            + " ("
                            + objectType.name()
                            + ')');
        }
        canonical.append("object:").append(objectType.name().length()).append(':').append(objectType.name()).append('{');
        for (ObjectComponent component : components) {
            String componentPath = name + '.' + component.name();
            ExpressionType componentExpressionType = inferType(componentPath, component.value());
            Object canonicalComponentValue = canonicalize(componentPath, componentExpressionType, component.value());
            String canonicalComponent = canonicalValue(componentPath, componentExpressionType, canonicalComponentValue);
            canonical.append(component.name().length())
                    .append(':')
                    .append(component.name())
                    .append('=')
                    .append(canonicalComponent.length())
                    .append(':')
                    .append(canonicalComponent)
                    .append(';');
        }
        canonical.append('}');
    }

    private static List<ObjectComponent> objectComponents(Class<?> valueType, Object value) {
        if (valueType.isRecord()) {
            List<ObjectComponent> components = new ArrayList<>();
            for (RecordComponent component : valueType.getRecordComponents()) {
                components.add(new ObjectComponent(component.getName(), readComponent(component, value)));
            }
            return components;
        }
        List<ObjectComponent> components = new ArrayList<>();
        for (Method method : valueType.getMethods()) {
            String propertyName = beanPropertyName(method);
            if (propertyName == null) {
                continue;
            }
            components.add(new ObjectComponent(propertyName, invokeBeanGetter(method, value)));
        }
        components.sort(Comparator.comparing(ObjectComponent::name));
        return components;
    }

    private static String beanPropertyName(Method method) {
        if (method.getParameterCount() != 0
                || method.getReturnType() == Void.TYPE
                || Modifier.isStatic(method.getModifiers())
                || method.getDeclaringClass() == Object.class) {
            return null;
        }
        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3 && !name.equals("getClass")) {
            return decapitalize(name.substring(3));
        }
        if (name.startsWith("is")
                && name.length() > 2
                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            return decapitalize(name.substring(2));
        }
        return null;
    }

    private static String decapitalize(String name) {
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0)) && Character.isUpperCase(name.charAt(1))) {
            return name;
        }
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static Object readComponent(RecordComponent component, Object value) {
        try {
            return component.getAccessor().invoke(value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException(
                    "cannot read ObjectType default record component: " + component.getName(),
                    exception);
        }
    }

    private static Object invokeBeanGetter(Method method, Object value) {
        try {
            return method.invoke(value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalArgumentException(
                    "cannot read ObjectType default bean property: " + method.getName(),
                    exception);
        }
    }

    private record ObjectComponent(String name, Object value) {
    }

}
