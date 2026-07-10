package com.runestone.expeval_mk3.api;

/**
 * Public expression type vocabulary consumed by semantic resolution.
 */
public sealed interface ExpressionType permits ScalarType, VectorType, CollectionType, MapType, ObjectType, NullType,
        UnknownType {
}
