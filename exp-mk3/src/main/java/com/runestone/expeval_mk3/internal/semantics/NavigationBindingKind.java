package com.runestone.expeval_mk3.internal.semantics;

public enum NavigationBindingKind {
    OBJECT_PROPERTY,
    OBJECT_METHOD,
    MAP_KEY,
    STRING_INDEX,
    STRING_SLICE,
    VECTOR_INDEX,
    VECTOR_SLICE,
    COLLECTION_INDEX,
    COLLECTION_SLICE,
    WILDCARD,
    FILTER,
    COLLECTION_OPERATION,
    SAFE_NULL_RECEIVER,
    DEFERRED_UNKNOWN_RECEIVER
}
