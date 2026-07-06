package com.runestone.expeval_mk3.internal.ast;

sealed interface CollectionOperationArgument extends AstNode permits LambdaCollectionOperationArgument,
        PositionalCollectionOperationArgument {
}
