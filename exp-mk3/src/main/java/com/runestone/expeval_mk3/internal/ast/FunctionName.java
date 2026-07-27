package com.runestone.expeval_mk3.internal.ast;

public record FunctionName(String value) {

    public FunctionName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Function name must not be blank");
        }
    }
}
