package com.runestone.expeval_mk3.api;

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
            case NullType ignored -> "NullType";
            case UnknownType ignored -> "UnknownType";
        };
    }
}
