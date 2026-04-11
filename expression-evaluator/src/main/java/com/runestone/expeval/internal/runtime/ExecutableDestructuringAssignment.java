package com.runestone.expeval.internal.runtime;

import java.util.List;
import java.util.Objects;

record ExecutableDestructuringAssignment(List<SymbolRef> targets, ExecutableNode value) implements ExecutableAssignment {

    ExecutableDestructuringAssignment {
        targets = List.copyOf(Objects.requireNonNull(targets, "targets must not be null"));
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("targets must not be empty");
        }
        Objects.requireNonNull(value, "value must not be null");
    }
}
