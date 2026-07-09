package com.runestone.expeval_mk3.internal.ast;

sealed interface SemanticAstBuildResult permits SemanticAstBuildFailure, SemanticAstBuildSuccess {
}
