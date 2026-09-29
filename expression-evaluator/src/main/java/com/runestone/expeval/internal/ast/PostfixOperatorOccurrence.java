package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.Objects;

public record PostfixOperatorOccurrence(PostfixOperator operator, SourceSpan sourceSpan) {

    public PostfixOperatorOccurrence {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
