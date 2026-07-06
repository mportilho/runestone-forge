package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record LambdaCollectionOperationArgument(NodeId id, SourceSpan sourceSpan, LambdaNode lambda)
        implements CollectionOperationArgument {

    LambdaCollectionOperationArgument {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(lambda, "lambda");
    }
}
