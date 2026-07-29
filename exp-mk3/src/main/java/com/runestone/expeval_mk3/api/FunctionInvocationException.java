package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * Reports a function provider failure encountered while executing a compiled expression: a
 * declared checked exception, an ordinary runtime exception, or a nonfatal error thrown by the
 * provider implementation. {@link VirtualMachineError}, {@link ThreadDeath}, and
 * {@link LinkageError} are fatal JVM conditions and are never wrapped; they propagate unchanged.
 */
public final class FunctionInvocationException extends RuntimeException {

    private final FunctionSignature functionSignature;

    private FunctionInvocationException(FunctionSignature functionSignature, String message, Throwable cause) {
        super(message, cause);
        this.functionSignature = functionSignature;
    }

    public static FunctionInvocationException providerFailure(FunctionDescriptor descriptor, Throwable cause) {
        Objects.requireNonNull(descriptor, "descriptor");
        Objects.requireNonNull(cause, "cause");
        FunctionSignature signature = descriptor.signature();
        return new FunctionInvocationException(
                signature,
                "function invocation failed: " + signature.canonical(),
                cause);
    }

    public FunctionSignature functionSignature() {
        return functionSignature;
    }
}
