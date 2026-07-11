package com.runestone.expeval_mk3.api;

import java.util.List;
import java.util.Objects;

final class ExpressionTypes {

    private ExpressionTypes() {
    }

    static String canonical(ExpressionType type) {
        return switch (type) {
            case ScalarType scalarType -> "ScalarType:" + scalarType.name();
            case VectorType vectorType -> "VectorType:" + canonical(vectorType.elementType());
            case CollectionType collectionType -> "CollectionType:" + canonical(collectionType.elementType());
            case MapType mapType -> "MapType:text:" + canonical(mapType.valueType());
            case ObjectType objectType -> "ObjectType:" + objectType.name();
        };
    }

    static List<ExpressionType> copyOf(List<ExpressionType> types, String name) {
        Objects.requireNonNull(types, name);
        for (int index = 0; index < types.size(); index++) {
            if (types.get(index) == null) {
                throw new NullPointerException(name + "[" + index + "]");
            }
        }
        return List.copyOf(types);
    }
}
