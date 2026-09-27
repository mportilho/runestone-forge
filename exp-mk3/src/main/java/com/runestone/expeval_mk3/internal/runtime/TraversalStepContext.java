package com.runestone.expeval_mk3.internal.runtime;

import com.runestone.expeval_mk3.api.SourceSpan;

/**
 * Binds evaluator-owned Java boundary adapters to the active SAFE execution without changing their
 * public method signatures. The binding is stack-disciplined so a provider's reentrant evaluation
 * restores the outer execution's allowance when it returns.
 */
public final class TraversalStepContext {
    private static final ThreadLocal<Frames> FRAMES = ThreadLocal.withInitial(Frames::new);

    private TraversalStepContext() {
    }

    public static void push(ExecutionScope scope, SourceSpan span) {
        FRAMES.get().push(scope, span);
    }

    public static void pop() {
        FRAMES.get().pop();
    }

    /** Charges the entry about to be converted by the currently bound adapter, if any. */
    public static void visit() {
        FRAMES.get().visit();
    }

    private static final class Frames {
        private ExecutionScope[] scopes = new ExecutionScope[4];
        private SourceSpan[] spans = new SourceSpan[4];
        private int depth;

        private void push(ExecutionScope scope, SourceSpan span) {
            if (depth == scopes.length) {
                int nextLength = scopes.length * 2;
                scopes = java.util.Arrays.copyOf(scopes, nextLength);
                spans = java.util.Arrays.copyOf(spans, nextLength);
            }
            scopes[depth] = scope;
            spans[depth] = span;
            depth++;
        }

        private void pop() {
            if (depth == 0) {
                throw new IllegalStateException("no traversal-step context to pop");
            }
            depth--;
            scopes[depth] = null;
            spans[depth] = null;
        }

        private void visit() {
            if (depth != 0) {
                scopes[depth - 1].visitTraversalStep(spans[depth - 1]);
            }
        }
    }
}
