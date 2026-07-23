package com.runestone.expeval_mk3.internal.ast;

sealed interface CallArgument permits ExpressionCallArgument, LambdaCallArgument {
}
