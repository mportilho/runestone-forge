package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record ExpressionCallArgument(ExpressionNode expression) implements CallArgument {

    ExpressionCallArgument {
        Objects.requireNonNull(expression, "expression");
    }
}
