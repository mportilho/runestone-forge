package com.runestone.expeval.api;

import java.util.Objects;

public final class ExpressionEvaluationException extends RuntimeException {

    private final String source;

    public ExpressionEvaluationException(String source, String code, String message, CompilationPosition position) {
        super(buildMessage(source, code, message, position));
        this.source = Objects.requireNonNull(source, "source must not be null");
    }

    public String source() {
        return source;
    }

    private static String buildMessage(String source, String code, String message, CompilationPosition position) {
        if (position == null) {
            return "evaluation failed for expression '" + source + "': " + code + ": " + message;
        }
        return "evaluation failed:\n\n" + SourcePointerFormatter.format(source, position, code, message);
    }
}
