package com.runestone.expeval.types;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Objects;

public final class ResolvedTypes {

    private ResolvedTypes() {
    }

    public static ResolvedType fromJavaType(Class<?> javaType) {
        Objects.requireNonNull(javaType, "javaType must not be null");
        if (javaType.isPrimitive()) {
            if (javaType == boolean.class) {
                return ScalarType.BOOLEAN;
            }
            return ScalarType.NUMBER;
        }
        if (Boolean.class.isAssignableFrom(javaType)) {
            return ScalarType.BOOLEAN;
        }
        if (Number.class.isAssignableFrom(javaType) || BigDecimal.class.isAssignableFrom(javaType)
            || BigInteger.class.isAssignableFrom(javaType)) {
            return ScalarType.NUMBER;
        }
        if (CharSequence.class.isAssignableFrom(javaType)) {
            return ScalarType.STRING;
        }
        if (LocalDate.class.isAssignableFrom(javaType)) {
            return ScalarType.DATE;
        }
        if (LocalTime.class.isAssignableFrom(javaType)) {
            return ScalarType.TIME;
        }
        if (LocalDateTime.class.isAssignableFrom(javaType)) {
            return ScalarType.DATETIME;
        }
        if (javaType.isArray() || Collection.class.isAssignableFrom(javaType)) {
            return VectorType.INSTANCE;
        }
        return UnknownType.INSTANCE;
    }

    public static ResolvedType merge(ResolvedType left, ResolvedType right) {
        Objects.requireNonNull(left, "left must not be null");
        Objects.requireNonNull(right, "right must not be null");
        if (left.equals(right)) {
            return left;
        }
        if (left == NullType.INSTANCE) {
            return right;
        }
        if (right == NullType.INSTANCE) {
            return left;
        }
        if (left == UnknownType.INSTANCE) {
            return right;
        }
        if (right == UnknownType.INSTANCE) {
            return left;
        }
        if (left instanceof CollectionType leftCol && right instanceof CollectionType rightCol) {
            return new CollectionType(merge(leftCol.elementType(), rightCol.elementType()));
        }
        if (left instanceof MapType leftMap && right instanceof MapType rightMap) {
            return new MapType(merge(leftMap.keyType(), rightMap.keyType()), merge(leftMap.valueType(), rightMap.valueType()));
        }
        // CollectionType + VectorType → VectorType (non-parametrized wins to avoid losing type info)
        if (left instanceof CollectionType && right == VectorType.INSTANCE
                || left == VectorType.INSTANCE && right instanceof CollectionType) {
            return VectorType.INSTANCE;
        }
        return UnknownType.INSTANCE;
    }
}
