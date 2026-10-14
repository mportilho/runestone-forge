package com.runestone.expeval.internal.ast;

import java.util.Objects;

public record SemanticAstBuildSuccess(ExpressionFileNode file) implements SemanticAstBuildResult {

    public SemanticAstBuildSuccess {
        Objects.requireNonNull(file, "file");
    }
}
