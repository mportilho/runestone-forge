package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record NumericConstantRestriction(SourceSpan sourceSpan, NumericConstantRestrictionKind kind) {

    public NumericConstantRestriction {
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(kind, "kind");
    }
}
