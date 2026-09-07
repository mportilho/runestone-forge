package com.runestone.expeval_mk3.internal.regex;

import java.util.Objects;

/** A literal regex compiled together with the exact official built-in operation that consumes it. */
public final class PreparedRegexCall {

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

    public Object execute(String value, String replacement) {
        return switch (operation) {
            case REPLACE_ALL -> pattern.replaceAll(value, Objects.requireNonNull(replacement, "replacement"));
            case SPLIT -> pattern.split(value);
        };
    }

    public boolean requiresReplacement() {
        return operation == Operation.REPLACE_ALL;
    }

    private enum Operation {
        REPLACE_ALL,
        SPLIT
    }
}
