package com.runestone.expeval2.grammar.language;

import java.util.Objects;

public record WarmupInput(String input, ExpressionResultType resultType) {

    public WarmupInput {
        Objects.requireNonNull(input, "input must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
    }
}
