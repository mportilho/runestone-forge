package com.runestone.expeval2.api;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * Snapshot of all identifier values read during evaluation, keyed by name.
     *
     * <p>Includes both user-supplied variables and built-in temporal identifiers
     * ({@code currDate}, {@code currTime}, {@code currDateTime}). When the same identifier
     * is read more than once, the last-read value is kept. Iteration order follows first
     * appearance.
     *
     * @return mutable {@link LinkedHashMap} (safe to post-process) of name → raw value
     */
    public Map<String, Object> variableSnapshot() {
        return events.stream()
                .filter(AuditEvent.VariableRead.class::isInstance)
                .map(AuditEvent.VariableRead.class::cast)
                .collect(Collectors.toMap(
                        AuditEvent.VariableRead::name,
                        AuditEvent.VariableRead::value,
                        (first, last) -> last,
                        LinkedHashMap::new
                ));
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
