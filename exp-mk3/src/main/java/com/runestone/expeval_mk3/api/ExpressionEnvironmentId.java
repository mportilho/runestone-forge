package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * Canonical, stable identity for an expression environment.
 */
public record ExpressionEnvironmentId(String value) {

    public ExpressionEnvironmentId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
