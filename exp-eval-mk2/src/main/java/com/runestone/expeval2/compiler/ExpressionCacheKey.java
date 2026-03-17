package com.runestone.expeval2.compiler;

import com.runestone.expeval2.environment.ExpressionEnvironmentId;
import com.runestone.expeval2.grammar.language.ExpressionResultType;

import java.util.Objects;

public record ExpressionCacheKey(
    String source,
    ExpressionEnvironmentId environmentId,
    ExpressionResultType resultType
) {

    public ExpressionCacheKey {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        Objects.requireNonNull(environmentId, "environmentId must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
    }
}
