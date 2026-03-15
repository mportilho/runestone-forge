package com.runestone.expeval2.grammar.exception;

import java.util.Objects;

public record SyntaxError(int line, int charPositionInLine, String message) {

    public SyntaxError {
        Objects.requireNonNull(message, "message must not be null");
    }
}
