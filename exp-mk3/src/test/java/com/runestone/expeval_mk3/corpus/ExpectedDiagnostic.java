package com.runestone.expeval_mk3.corpus;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.List;
import java.util.Objects;

record ExpectedDiagnostic(String category, String code, List<SourceSpan> spans) implements ExpectedOutcome {

    ExpectedDiagnostic {
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(code, "code");
        spans = List.copyOf(Objects.requireNonNull(spans, "spans"));
    }

    SourceSpan requiredSpan() {
        if (spans.isEmpty()) {
            throw new IllegalStateException("diagnostic does not declare a source span");
        }
        return spans.getFirst();
    }
}
