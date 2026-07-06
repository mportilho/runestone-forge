package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

enum CurrentTemporalValueKind {
    DATE("currDate"),
    TIME("currTime"),
    DATETIME("currDateTime");

    private final String canonicalName;

    CurrentTemporalValueKind(String canonicalName) {
        this.canonicalName = Objects.requireNonNull(canonicalName, "canonicalName");
    }

    String canonicalName() {
        return canonicalName;
    }
}
