package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Objects;

public record PostfixOperatorOccurrence(PostfixOperator operator, SourceSpan sourceSpan) {

    public PostfixOperatorOccurrence {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
