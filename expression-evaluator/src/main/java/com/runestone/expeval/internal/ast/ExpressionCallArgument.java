package com.runestone.expeval.internal.ast;

import java.util.Objects;

public record ExpressionCallArgument(ExpressionNode expression) implements CallArgument {

    public ExpressionCallArgument {
        Objects.requireNonNull(expression, "expression");
    }
}
