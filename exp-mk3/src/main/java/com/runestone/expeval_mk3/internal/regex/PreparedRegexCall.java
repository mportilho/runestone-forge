package com.runestone.expeval_mk3.internal.regex;

import com.runestone.expeval_mk3.api.SourceSpan;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import java.util.Objects;

/** A literal regex compiled together with the exact official built-in operation that consumes it. */
public final class PreparedRegexCall {

    private static final int CALL_UNITS = 64;
    private final Operation operation;
    private final LinearRegex pattern;

    private PreparedRegexCall(Operation operation, LinearRegex pattern) {
        this.operation = Objects.requireNonNull(operation, "operation");
        this.pattern = Objects.requireNonNull(pattern, "pattern");
    }

    public static PreparedRegexCall replaceAll(LinearRegex pattern) {
        return new PreparedRegexCall(Operation.REPLACE_ALL, pattern);
    }

    public static PreparedRegexCall split(LinearRegex pattern) {
        return new PreparedRegexCall(Operation.SPLIT, pattern);
    }

    public Object execute(String value, String replacement, ExecutionScope scope, SourceSpan span) {
        return switch (operation) {
            case REPLACE_ALL -> scope.replaceAll(pattern, value, Objects.requireNonNull(replacement, "replacement"), span);
            case SPLIT -> scope.split(pattern, value, span);
        };
    }

    public boolean requiresReplacement() {
        return operation == Operation.REPLACE_ALL;
    }

    /** Conservative payload retained by the operation marker and its prepared pattern. */
    public int estimatedRetainedWeight() {
        return (int) Math.min(Integer.MAX_VALUE, (long) CALL_UNITS + pattern.estimatedRetainedWeight());
    }

    private enum Operation {
        REPLACE_ALL,
        SPLIT
    }
}
