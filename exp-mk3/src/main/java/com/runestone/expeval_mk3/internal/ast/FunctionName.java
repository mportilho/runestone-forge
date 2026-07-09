package com.runestone.expeval_mk3.internal.ast;

record FunctionName(String value) {

    FunctionName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Function name must not be blank");
        }
    }
}
