package com.runestone.expeval2.internal.grammar;

import java.util.Objects;

record WarmupInput(String input, ExpressionResultType resultType) {

    public WarmupInput {
        Objects.requireNonNull(input, "input must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
    }
}
