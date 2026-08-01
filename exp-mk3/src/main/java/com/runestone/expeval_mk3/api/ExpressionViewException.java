package com.runestone.expeval_mk3.api;

import java.util.Objects;

/**
 * Reports an incompatible {@code CompiledExpression} view selection. Raised from the view selector
 * ({@code asResult}/{@code asMath}/{@code asLogical}/{@code asAssignments}) itself, before any override is
 * prepared or the plan executes, since compatibility is decided from the plan's static shape alone.
 */
public final class ExpressionViewException extends IllegalStateException {

    public enum Reason {
        /** The plan has no final result expression, only assignments. */
        NO_RESULT_EXPRESSION,
        /** The plan has no assignments for {@code asAssignments} to expose. */
        NO_ASSIGNMENTS,
        /** The result type (or a type nested in it) is not publicly exposable. */
        TYPE_NOT_PUBLICLY_EXPOSABLE,
        /** The result type does not match the scalar type the view requires. */
        TYPE_MISMATCH,
        /** The known number of assignment entries exceeds {@code maxMaterializedSize}. */
        MATERIALIZATION_LIMIT_EXCEEDED
    }

    private final Reason reason;
    private final ExpressionType foundType;
    private final SourceSpan sourceSpan;

    private ExpressionViewException(Reason reason, ExpressionType foundType, SourceSpan sourceSpan) {
        super(message(reason, foundType));
        this.reason = reason;
        this.foundType = foundType;
        this.sourceSpan = sourceSpan;
    }

    static ExpressionViewException of(Reason reason, ExpressionType foundType, SourceSpan sourceSpan) {
        return new ExpressionViewException(Objects.requireNonNull(reason, "reason"), foundType, sourceSpan);
    }

    public Reason reason() {
        return reason;
    }

    /**
     * The expression's resolved type when one exists; {@code null} for {@link Reason#NO_RESULT_EXPRESSION},
     * {@link Reason#NO_ASSIGNMENTS}, and {@link Reason#MATERIALIZATION_LIMIT_EXCEEDED}.
     */
    public ExpressionType foundType() {
        return foundType;
    }

    /**
     * The result expression's source position when one exists; {@code null} otherwise.
     */
    public SourceSpan sourceSpan() {
        return sourceSpan;
    }

    private static String message(Reason reason, ExpressionType foundType) {
        return switch (reason) {
            case NO_RESULT_EXPRESSION ->
                    "expression view requires a final result expression, but the file has only assignments";
            case NO_ASSIGNMENTS -> "assignments view requires at least one assignment, but the file has none";
            case TYPE_NOT_PUBLICLY_EXPOSABLE -> "expression result type is not publicly exposable: " + foundType;
            case TYPE_MISMATCH -> "expression result type is incompatible with the requested view: " + foundType;
            case MATERIALIZATION_LIMIT_EXCEEDED ->
                    "assignments view has more entries than maxMaterializedSize allows";
        };
    }
}
