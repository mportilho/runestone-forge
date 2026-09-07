package com.runestone.expeval_mk3.corpus;

import java.util.List;
import java.util.Objects;

record ExpectedDiagnostics(List<ExpectedDiagnostic> diagnostics) implements ExpectedOutcome {

    ExpectedDiagnostics {
        diagnostics = List.copyOf(Objects.requireNonNull(diagnostics, "diagnostics"));
        if (diagnostics.isEmpty()) {
            throw new IllegalArgumentException("diagnostics must not be empty");
        }
    }
}
