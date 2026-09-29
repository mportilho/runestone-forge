package com.runestone.expeval.corpus;

import com.runestone.expeval.api.SourceSpan;

import java.util.List;
import java.util.Objects;

record ExpectedDiagnostic(String category, String code, List<SourceSpan> spans) {

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
