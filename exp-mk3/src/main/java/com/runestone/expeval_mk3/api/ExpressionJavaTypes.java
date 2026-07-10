package com.runestone.expeval_mk3.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

final class ExpressionJavaTypes {

    private ExpressionJavaTypes() {
    }

    static Class<?> valueType(ExpressionType expressionType) {
        return switch (expressionType) {
            case ScalarType scalarType -> scalarValueType(scalarType);
            case VectorType ignored -> List.class;
            case CollectionType ignored -> List.class;
            case MapType ignored -> Map.class;
            case UnknownType ignored -> Object.class;
            case NullType ignored -> null;
            case ObjectType ignored -> Object.class;
        };
    }

    static Class<?> scalarValueType(ScalarType scalarType) {
        return switch (scalarType) {
            case NUMBER -> BigDecimal.class;
            case BOOLEAN -> Boolean.class;
            case STRING -> String.class;
            case DATE -> LocalDate.class;
            case TIME -> LocalTime.class;
            case DATETIME -> LocalDateTime.class;
        };
    }

    static Class<?> boxed(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        return switch (type.getName()) {
            case "boolean" -> Boolean.class;
            case "byte" -> Byte.class;
            case "char" -> Character.class;
            case "short" -> Short.class;
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "float" -> Float.class;
            case "double" -> Double.class;
            case "void" -> Void.class;
            default -> type;
        };
    }
}
