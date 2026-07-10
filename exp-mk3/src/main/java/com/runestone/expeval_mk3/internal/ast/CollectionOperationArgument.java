package com.runestone.expeval_mk3.internal.ast;

sealed interface CollectionOperationArgument permits LambdaCollectionOperationArgument,
        PositionalCollectionOperationArgument {
}
