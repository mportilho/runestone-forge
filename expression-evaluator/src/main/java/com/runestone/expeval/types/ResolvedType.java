package com.runestone.expeval.types;

public sealed interface ResolvedType permits ScalarType, UnknownType, VectorType, ObjectType, NullType {
}
