package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.api.ExpressionAuditTrace;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lightweight, single-use collector of {@link AuditEvent}s for one expression evaluation.
 *
 * <p>Not thread-safe — expression evaluation is single-threaded per scope.
 * Create one instance per {@code computeWithAudit()} call and discard after {@link #buildTrace()}.
 *
 * <p>The {@code initialCapacity} parameter should be the maximum number of audit events that can
 * be emitted by the expression (computed statically from the {@code ExecutionPlan} by
 * {@link ExpressionRuntimeSupport}). Pre-sizing avoids ArrayList growth and the defensive
 * {@code List.copyOf} in {@link #buildTrace()} is replaced by an unmodifiable wrapper.
 */
final class AuditCollector {

    private final long startNanos = System.nanoTime();
    private final List<AuditEvent> events;

    AuditCollector(int initialCapacity) {
        this.events = new ArrayList<>(initialCapacity);
    }

    void record(AuditEvent event) {
        events.add(event);
    }

    ExpressionAuditTrace buildTrace() {
        return new ExpressionAuditTrace(Collections.unmodifiableList(events), Duration.ofNanos(System.nanoTime() - startNanos));
    }
}
