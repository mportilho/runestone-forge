package com.runestone.expeval_mk3.api;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

final class JavaMemberTypes {

    private static final Set<Class<?>> NUMERIC_TYPES = Set.of(
            BigDecimal.class,
            byte.class,
            Byte.class,
            short.class,
            Short.class,
            int.class,
            Integer.class,
            long.class,
            Long.class,
            float.class,
            Float.class,
            double.class,
            Double.class);

    private JavaMemberTypes() {
    }

    static RuntimeNullability returnNullability() {
        return RuntimeNullability.NEVER_NULL;
    }

    static ExpressionType expressionType(Type genericType, Class<?> rawType, boolean arrayAsCollection) {
        return tryExpressionType(genericType, rawType, arrayAsCollection)
                .orElseThrow(() -> new IllegalArgumentException("unsupported Java member type: " + rawType.getName()));
    }

    static Optional<ExpressionType> tryExpressionType(Type genericType, Class<?> rawType, boolean arrayAsCollection) {
        if (rawType == void.class) {
            return Optional.empty();
        }
        if (rawType.isArray()) {
            if (arrayAsCollection) {
                Class<?> componentType = rawType.getComponentType();
                return tryExpressionType(componentType, componentType, false)
                        .map(CollectionType::new);
            }
            return Optional.empty();
        }
        if (Optional.class.isAssignableFrom(rawType)) {
            return Optional.empty();
        }
        if (NUMERIC_TYPES.contains(rawType)) {
            return Optional.of(ScalarType.NUMBER);
        }
        if (rawType == boolean.class || rawType == Boolean.class) {
            return Optional.of(ScalarType.BOOLEAN);
        }
        if (rawType == String.class) {
            return Optional.of(ScalarType.STRING);
        }
        if (rawType == LocalDate.class) {
            return Optional.of(ScalarType.DATE);
        }
        if (rawType == LocalTime.class) {
            return Optional.of(ScalarType.TIME);
        }
        if (rawType == LocalDateTime.class) {
            return Optional.of(ScalarType.DATETIME);
        }
        if (rawType == Object.class) {
            return Optional.empty();
        }
        if (Map.class.isAssignableFrom(rawType)) {
            return mapType(genericType);
        }
        if (Collection.class.isAssignableFrom(rawType) || Iterable.class.isAssignableFrom(rawType)) {
            return collectionType(genericType);
        }
        if (rawType.isPrimitive()) {
            return Optional.empty();
        }
        return Optional.of(new ObjectType(rawType.getName()));
    }

    private static Optional<ExpressionType> mapType(Type genericType) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return Optional.empty();
        }
        Type keyType = parameterizedType.getActualTypeArguments()[0];
        if (keyType != String.class) {
            return Optional.empty();
        }
        Type valueType = parameterizedType.getActualTypeArguments()[1];
        if (!(valueType instanceof Class<?> valueClass)) {
            return Optional.empty();
        }
        return tryExpressionType(valueClass, valueClass, false)
                .map(MapType::new);
    }

    private static Optional<ExpressionType> collectionType(Type genericType) {
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return Optional.empty();
        }
        Type elementType = parameterizedType.getActualTypeArguments()[0];
        if (elementType instanceof WildcardType wildcardType) {
            if (wildcardType.getLowerBounds().length == 0
                    && wildcardType.getUpperBounds().length == 1
                    && wildcardType.getUpperBounds()[0] == Object.class) {
                return Optional.empty();
            }
            return Optional.empty();
        }
        if (!(elementType instanceof Class<?> elementClass)) {
            return Optional.empty();
        }
        return tryExpressionType(elementClass, elementClass, false)
                .map(CollectionType::new);
    }
}
