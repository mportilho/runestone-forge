package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record PostfixOperatorOccurrence(PostfixOperator operator, SourceSpan sourceSpan) {

    PostfixOperatorOccurrence {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
