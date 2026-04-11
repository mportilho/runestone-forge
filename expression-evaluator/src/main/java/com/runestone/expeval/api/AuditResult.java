package com.runestone.expeval.api;

import java.util.Objects;

/**
 * Pairs the computed expression result with a full audit trace of the evaluation.
 *
 * @param <T>   result type ({@link java.math.BigDecimal} for math, {@link Boolean} for logical)
 * @param value computed result
 * @param trace execution audit trace
 */
public record AuditResult<T>(T value, ExpressionAuditTrace trace) {
    public AuditResult {
        Objects.requireNonNull(trace, "trace must not be null");
    }
}
