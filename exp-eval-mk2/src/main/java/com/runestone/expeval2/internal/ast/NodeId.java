package com.runestone.expeval2.internal.ast;

import java.util.Objects;

public record NodeId(String value) {

    public NodeId {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
