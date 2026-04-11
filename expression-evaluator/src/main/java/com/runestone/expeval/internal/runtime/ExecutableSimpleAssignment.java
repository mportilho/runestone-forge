package com.runestone.expeval.internal.runtime;

import java.util.Objects;

record ExecutableSimpleAssignment(SymbolRef target, ExecutableNode value) implements ExecutableAssignment {

    ExecutableSimpleAssignment {
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(value, "value must not be null");
    }
}
