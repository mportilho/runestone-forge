package com.runestone.expeval2.internal.runtime;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class ExecutionScope {

    private final Map<SymbolRef, RuntimeValue> values;
    private final boolean mutable;

    private ExecutionScope(Map<SymbolRef, RuntimeValue> values, boolean mutable) {
        this.values = Objects.requireNonNull(values, "values must not be null");
        this.mutable = mutable;
    }

    static ExecutionScope fromIsolated(Map<SymbolRef, RuntimeValue> freshValues) {
        return new ExecutionScope(Objects.requireNonNull(freshValues, "freshValues must not be null"), true);
    }

    /**
     * Creates a read-only scope backed by a shared map.
     * <p>
     * Use only when the expression has no assignments. Calling {@link #assign} on the
     * returned scope throws {@link IllegalStateException}.
     */
    static ExecutionScope readOnly(Map<SymbolRef, RuntimeValue> sharedValues) {
        return new ExecutionScope(Objects.requireNonNull(sharedValues, "sharedValues must not be null"), false);
    }

    public Optional<RuntimeValue> find(SymbolRef symbolRef) {
        Objects.requireNonNull(symbolRef, "symbolRef must not be null");
        return Optional.ofNullable(values.get(symbolRef));
    }

    public void assign(SymbolRef symbolRef, RuntimeValue value) {
        if (!mutable) {
            throw new IllegalStateException("assign() is not allowed on a read-only ExecutionScope");
        }
        values.put(
            Objects.requireNonNull(symbolRef, "symbolRef must not be null"),
            Objects.requireNonNull(value, "value must not be null")
        );
    }
}
