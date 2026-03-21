package com.runestone.expeval2.api;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable audit trace of a single expression evaluation.
 *
 * <p>Contains all {@link AuditEvent}s emitted in evaluation order, plus the elapsed wall-clock
 * time of the evaluation. Convenience views are provided for the most common audit queries.
 *
 * @param events         all events in evaluation order
 * @param evaluationTime wall-clock duration of the evaluation
 */
public record ExpressionAuditTrace(List<AuditEvent> events, Duration evaluationTime) {

    public ExpressionAuditTrace {
        Objects.requireNonNull(events, "events must not be null");
        Objects.requireNonNull(evaluationTime, "evaluationTime must not be null");
    }

    /**
     * Snapshot of all variable values seen during evaluation, keyed by name.
     *
     * <p>Includes assigned variables (from simple and destructuring assignments),
     * user-supplied variables, and built-in temporal identifiers
     * ({@code currDate}, {@code currTime}, {@code currDateTime}).
     * Events are processed in evaluation order: assignments are recorded first,
     * and subsequent reads overwrite the value for the same name. Iteration order
     * follows first appearance.
     *
     * @return mutable {@link LinkedHashMap} (safe to post-process) of name → raw value
     */
    public Map<String, Object> variableSnapshot() {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        for (AuditEvent event : events) {
            switch (event) {
                case AuditEvent.AssignmentEvent a -> snapshot.put(a.targetName(), a.newValue());
                case AuditEvent.VariableRead r    -> snapshot.put(r.name(), r.value());
                case AuditEvent.FunctionCall ignored -> {}
            }
        }
        return snapshot;
    }

    /**
     * Ordered list of all function calls made during the evaluation, with coerced inputs and raw output.
     */
    public List<AuditEvent.FunctionCall> functionCalls() {
        return events.stream()
                .filter(AuditEvent.FunctionCall.class::isInstance)
                .map(AuditEvent.FunctionCall.class::cast)
                .toList();
    }
}
