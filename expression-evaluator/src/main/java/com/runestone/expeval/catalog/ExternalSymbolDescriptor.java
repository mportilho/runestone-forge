package com.runestone.expeval.catalog;

import com.runestone.expeval.types.ResolvedType;

import java.util.Objects;

public record ExternalSymbolDescriptor(
    String name,
    ResolvedType declaredType,
    Object defaultValue,
    boolean overridable
) {

    public ExternalSymbolDescriptor {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(declaredType, "declaredType must not be null");
    }
}
