package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;

import java.util.Objects;

record CurrentItemResolvedSymbol(
        int currentItemDepth,
        ExpressionType type,
        int slot) implements ResolvedSymbol {

    CurrentItemResolvedSymbol {
        if (currentItemDepth < 1) {
            throw new IllegalArgumentException("currentItemDepth must be one or greater");
        }
        Objects.requireNonNull(type, "type");
        if (slot < 0) {
            throw new IllegalArgumentException("slot must not be negative");
        }
    }

    @Override
    public String name() {
        return "@" + currentItemDepth;
    }

    @Override
    public ResolvedSymbolKind kind() {
        return ResolvedSymbolKind.CURRENT_ITEM;
    }
}
