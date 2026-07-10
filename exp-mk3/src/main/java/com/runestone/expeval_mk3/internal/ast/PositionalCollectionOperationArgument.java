package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record PositionalCollectionOperationArgument(ExpressionNode expression) implements CollectionOperationArgument {

    PositionalCollectionOperationArgument {
        Objects.requireNonNull(expression, "expression");
    }
}
