package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record MemberName(String value, SourceSpan sourceSpan) {

    MemberName {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}
