package com.runestone.expeval2.types;

public sealed interface ResolvedType permits ScalarType, UnknownType, VectorType, ObjectType, NullType {
}
