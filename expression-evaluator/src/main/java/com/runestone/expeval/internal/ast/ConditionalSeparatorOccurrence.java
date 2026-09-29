package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.Objects;

record ConditionalSeparatorOccurrence(ConditionalSeparator separator, SourceSpan sourceSpan) {

    ConditionalSeparatorOccurrence {
        Objects.requireNonNull(separator, "separator");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
