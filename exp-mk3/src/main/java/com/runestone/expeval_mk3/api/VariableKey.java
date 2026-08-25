package com.runestone.expeval_mk3.api;

import java.util.Objects;

public record VariableKey(String name, VariableOrigin origin) {

    public VariableKey {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(origin, "origin");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
