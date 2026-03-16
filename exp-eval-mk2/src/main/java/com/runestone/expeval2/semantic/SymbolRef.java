package com.runestone.expeval2.semantic;

import java.util.Objects;

public record SymbolRef(
    String name,
    SymbolKind kind
) {

    public SymbolRef {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(kind, "kind must not be null");
    }
}
