package com.runestone.expeval2.internal.runtime;

import java.util.Objects;

record ExecutableDynamicLiteral(DynamicInstant kind) implements ExecutableNode {

    ExecutableDynamicLiteral {
        Objects.requireNonNull(kind, "kind must not be null");
    }
}
