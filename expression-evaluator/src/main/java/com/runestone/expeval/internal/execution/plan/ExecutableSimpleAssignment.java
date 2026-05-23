package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.internal.semantic.SymbolRef;

import java.util.Objects;

public record ExecutableSimpleAssignment(SymbolRef target, ExecutableNode value) implements ExecutableAssignment {

    public ExecutableSimpleAssignment {
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(value, "value must not be null");
    }
}
