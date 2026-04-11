package com.runestone.expeval.catalog;

import com.runestone.expeval.types.ResolvedType;

import java.lang.invoke.MethodHandle;
import java.util.Objects;

public record PropertyDescriptor(
        String name,
        MethodHandle getter,
        ResolvedType resolvedType
) {

    public PropertyDescriptor {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(getter, "getter must not be null");
        Objects.requireNonNull(resolvedType, "resolvedType must not be null");
    }
}
