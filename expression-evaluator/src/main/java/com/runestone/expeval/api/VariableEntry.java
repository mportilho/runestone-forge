package com.runestone.expeval.api;

import java.util.Objects;

public record VariableEntry(VariableKey key, Object value) {

    public VariableEntry {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }
}
