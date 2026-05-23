package com.runestone.expeval.internal.execution.plan;

import java.util.Objects;

public record ExecutableDynamicLiteral(DynamicInstant kind) implements ExecutableNode {

    public ExecutableDynamicLiteral {
        Objects.requireNonNull(kind, "kind must not be null");
    }
}
