package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.api.AuditEvent;
import com.runestone.expeval2.api.ExpressionAuditTrace;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight, single-use collector of {@link AuditEvent}s for one expression evaluation.
 *
 * <p>Not thread-safe — expression evaluation is single-threaded per scope.
 * Create one instance per {@code computeWithAudit()} call and discard after {@link #buildTrace()}.
 */
final class AuditCollector {

    private final long startNanos = System.nanoTime();
    private final List<AuditEvent> events = new ArrayList<>(16);
    private int callDepth = 0;

    void record(AuditEvent event) {
        events.add(event);
    }

    void enterCall() {
        callDepth++;
    }

    void exitCall() {
        callDepth--;
    }

    int callDepth() {
        return callDepth;
    }

    ExpressionAuditTrace buildTrace() {
        return new ExpressionAuditTrace(List.copyOf(events), Duration.ofNanos(System.nanoTime() - startNanos));
    }
}
