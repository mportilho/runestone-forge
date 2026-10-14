package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.SourceSpan;

/** Signals a resource violation in a successfully evaluated constant candidate. */
public final class ConstantShapeException extends RuntimeException {
    private final SourceSpan span;

    public ConstantShapeException(String message, SourceSpan span) {
        super(message);
        this.span = span;
    }

    public SourceSpan span() {
        return span;
    }
}
