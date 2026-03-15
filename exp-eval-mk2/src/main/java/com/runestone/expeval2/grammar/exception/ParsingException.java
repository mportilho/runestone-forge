package com.runestone.expeval2.grammar.exception;

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
            return "failed to parse input: %s".formatted(input);
        }

        SyntaxError firstError = errors.getFirst();
        return "failed to parse input at %d:%d - %s".formatted(
                firstError.line(),
                firstError.charPositionInLine(),
                firstError.message()
        );
    }
}
