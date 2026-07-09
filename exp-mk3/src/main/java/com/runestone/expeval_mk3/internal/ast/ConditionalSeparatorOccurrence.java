package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record ConditionalSeparatorOccurrence(ConditionalSeparator separator, SourceSpan sourceSpan) {

    ConditionalSeparatorOccurrence {
        Objects.requireNonNull(separator, "separator");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
