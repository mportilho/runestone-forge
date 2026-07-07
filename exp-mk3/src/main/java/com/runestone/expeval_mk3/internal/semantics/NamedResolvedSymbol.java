package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;

import java.util.Objects;

record NamedResolvedSymbol(
        String name,
        ResolvedSymbolKind kind,
        ExpressionType type,
        int slot) implements ResolvedSymbol {

    NamedResolvedSymbol {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(type, "type");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (kind == ResolvedSymbolKind.CURRENT_ITEM) {
            throw new IllegalArgumentException("named symbols cannot use current item kind");
        }
        if (slot < 0) {
            throw new IllegalArgumentException("slot must not be negative");
        }
    }
}
