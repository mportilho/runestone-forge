package com.runestone.expeval2.catalog;

import com.runestone.expeval2.runtime.RuntimeValue;
import com.runestone.expeval2.semantic.ResolvedType;

import java.util.Objects;

public record ExternalSymbolDescriptor(
    String name,
    ResolvedType declaredType,
    RuntimeValue defaultValue,
    boolean overridable
) {

    public ExternalSymbolDescriptor {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(declaredType, "declaredType must not be null");
        Objects.requireNonNull(defaultValue, "defaultValue must not be null");
    }
}
