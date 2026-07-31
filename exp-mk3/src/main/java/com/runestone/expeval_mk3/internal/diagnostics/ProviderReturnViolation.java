package com.runestone.expeval_mk3.internal.diagnostics;

import java.util.Objects;

/**
 * Marks a function-provider return-contract violation (null, incompatible type, invalid container,
 * or over-limit) detected while {@code ProviderMethodAdapter}'s converted {@code MethodHandle} runs.
 * Thrown from inside {@code MethodHandle.invokeWithArguments}, it lets the runtime invocation boundary
 * tell a provider-thrown failure apart from a provider-returned contract violation and classify each
 * with its own stable {@link DiagnosticCode} instead of leaking the raw conversion exception.
 */
public final class ProviderReturnViolation extends RuntimeException {

    private final DiagnosticCode code;

    public ProviderReturnViolation(DiagnosticCode code, String message) {
        super(message);
        this.code = Objects.requireNonNull(code, "code");
    }

    public DiagnosticCode code() {
        return code;
    }
}
