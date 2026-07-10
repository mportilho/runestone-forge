package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record LambdaCollectionOperationArgument(LambdaNode lambda) implements CollectionOperationArgument {

    LambdaCollectionOperationArgument {
        Objects.requireNonNull(lambda, "lambda");
    }
}
