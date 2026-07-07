package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record ResidualTypeCheck(DiagnosticCode code, SourceSpan sourceSpan, String description) {

    public ResidualTypeCheck {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(description, "description");
    }
}
