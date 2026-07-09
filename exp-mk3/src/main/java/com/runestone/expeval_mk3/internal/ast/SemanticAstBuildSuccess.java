package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record SemanticAstBuildSuccess(ExpressionFileNode file) implements SemanticAstBuildResult {

    SemanticAstBuildSuccess {
        Objects.requireNonNull(file, "file");
    }
}
