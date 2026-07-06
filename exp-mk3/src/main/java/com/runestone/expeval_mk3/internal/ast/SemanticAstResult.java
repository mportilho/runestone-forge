package com.runestone.expeval_mk3.internal.ast;

sealed interface SemanticAstResult permits SemanticAstSuccess, SemanticAstFailure {
}
