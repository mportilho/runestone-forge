package com.runestone.expeval.internal.runtime;

import java.util.Objects;

public final class SymbolRef {
    private final String name;
    private final SymbolKind kind;
    private int index = -1;

    public SymbolRef(String name, SymbolKind kind) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        this.name = name;
        this.kind = Objects.requireNonNull(kind, "kind must not be null");
    }

    public String name() {
        return name;
    }

    public SymbolKind kind() {
        return kind;
    }

    public int index() {
        return index;
    }

    void setIndex(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must be >= 0, got: " + index);
        }
        if (this.index != -1) {
            throw new IllegalStateException(
                    "index already assigned for symbol '" + name + "': " + this.index);
        }
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolRef symbolRef = (SymbolRef) o;
        return Objects.equals(name, symbolRef.name) && kind == symbolRef.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, kind);
    }
}
