package com.runestone.expeval_mk3.api;

import java.util.Objects;

public record VariableEntry(VariableKey key, Object value) {

    public VariableEntry {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
    }
}
