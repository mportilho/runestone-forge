package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record SemanticAstSuccess(ExpressionFileNode file) implements SemanticAstResult {

    SemanticAstSuccess {
        Objects.requireNonNull(file, "file");
    }
}
