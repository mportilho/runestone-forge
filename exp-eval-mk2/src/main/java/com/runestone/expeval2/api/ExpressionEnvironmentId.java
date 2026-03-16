package com.runestone.expeval2.api;

public record ExpressionEnvironmentId(String value) {

    public ExpressionEnvironmentId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
