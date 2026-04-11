package com.runestone.expeval.internal.runtime;

import java.util.Objects;

record ExecutableDynamicLiteral(DynamicInstant kind) implements ExecutableNode {

    ExecutableDynamicLiteral {
        Objects.requireNonNull(kind, "kind must not be null");
    }
}
