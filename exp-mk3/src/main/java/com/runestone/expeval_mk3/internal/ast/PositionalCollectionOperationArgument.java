package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record PositionalCollectionOperationArgument(NodeId id, SourceSpan sourceSpan, ExpressionNode expression)
        implements CollectionOperationArgument {

    PositionalCollectionOperationArgument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(expression, "expression");
    }
}
