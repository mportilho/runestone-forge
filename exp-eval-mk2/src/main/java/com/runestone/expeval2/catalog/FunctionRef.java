package com.runestone.expeval2.catalog;

public record FunctionRef(
    String name,
    int arity
) {

    public FunctionRef {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (arity < 0) {
            throw new IllegalArgumentException("arity must not be negative");
        }
    }
}
