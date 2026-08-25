package com.runestone.expeval_mk3.api;

import java.util.Objects;

public record CalculationKey(int nodeId, SourceSpan sourceSpan, CalculationKind kind, String name) {

    public CalculationKey {
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(name, "name");
        if (nodeId < 0) {
            throw new IllegalArgumentException("nodeId must not be negative");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
