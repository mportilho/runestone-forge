package com.runestone.expeval2.internal.runtime;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class ExecutionScope {

    private final Map<SymbolRef, RuntimeValue> values;

    private ExecutionScope(Map<SymbolRef, RuntimeValue> values) {
        this.values = new HashMap<>(Objects.requireNonNull(values, "values must not be null"));
    }

    public static ExecutionScope from(Map<SymbolRef, RuntimeValue> baseValues) {
        return new ExecutionScope(baseValues);
    }

    public Optional<RuntimeValue> find(SymbolRef symbolRef) {
        Objects.requireNonNull(symbolRef, "symbolRef must not be null");
        return Optional.ofNullable(values.get(symbolRef));
    }

    public void assign(SymbolRef symbolRef, RuntimeValue value) {
        values.put(Objects.requireNonNull(symbolRef, "symbolRef must not be null"), Objects.requireNonNull(value, "value must not be null"));
    }
}
