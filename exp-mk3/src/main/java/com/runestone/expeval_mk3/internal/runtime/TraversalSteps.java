package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;

/** Keeps traversal accounting out of scalar scope dispatch. */
public final class TraversalSteps {
    private TraversalSteps() {
    }

    public static void visit(ExecutionScope scope, SourceSpan span) {
        if (scope instanceof TraversalLimitedExecutionScope limited) {
            limited.visit(span);
        }
    }

    public static boolean isLimited(ExecutionScope scope) {
        return scope instanceof TraversalLimitedExecutionScope;
    }
}
