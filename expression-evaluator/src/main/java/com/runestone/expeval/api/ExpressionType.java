package com.runestone.expeval.api;

/**
 * Public expression type vocabulary consumed by semantic resolution.
 */
public sealed interface ExpressionType permits ScalarType, CollectionType, MapType, ObjectType {
}
