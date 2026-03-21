package com.runestone.expeval2.internal.grammar;

import java.util.List;
import java.util.Objects;

public final class ParsingException extends RuntimeException {

    private final List<SyntaxError> errors;

    public ParsingException(String input, List<SyntaxError> errors) {
        super(buildMessage(input, errors));
        this.errors = List.copyOf(Objects.requireNonNull(errors, "errors must not be null"));
    }

    public List<SyntaxError> errors() {
        return errors;
    }

    private static String buildMessage(String input, List<SyntaxError> errors) {
        if (errors.isEmpty()) {
            return "failed to parse expression:\n\n  " + input;
        }
        SyntaxError first = errors.getFirst();
        String[] lines = input.split("\n", -1);
        int lineIdx = first.line() - 1;
        String sourceLine = (lineIdx >= 0 && lineIdx < lines.length) ? lines[lineIdx] : input;
        String pointer = " ".repeat(Math.max(0, first.charPositionInLine())) + "^";
        return "failed to parse expression:\n\n  %s\n  %s\n  syntax error at %d:%d \u2014 %s".formatted(
            sourceLine, pointer, first.line(), first.charPositionInLine(), first.message()
        );
    }
}
