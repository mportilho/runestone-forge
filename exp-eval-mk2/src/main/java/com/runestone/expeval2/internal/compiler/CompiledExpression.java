package com.runestone.expeval2.internal.compiler;

import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.internal.semantic.SemanticModel;

import java.util.Objects;

public record CompiledExpression(
    String source,
    ExpressionResultType resultType,
    SemanticModel semanticModel
) {

    public CompiledExpression {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(semanticModel, "semanticModel must not be null");
    }
}
