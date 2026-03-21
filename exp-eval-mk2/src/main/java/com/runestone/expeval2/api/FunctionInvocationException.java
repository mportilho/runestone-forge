package com.runestone.expeval2.api;

import java.util.Objects;

public final class FunctionInvocationException extends RuntimeException {

    private final String functionName;

    public FunctionInvocationException(String functionName, Throwable cause) {
        super("failed to invoke function '" + Objects.requireNonNull(functionName, "functionName must not be null") + "'", cause);
        this.functionName = functionName;
    }

    public String functionName() {
        return functionName;
    }
}
