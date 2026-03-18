package com.runestone.expeval2.internal.runtime;

import java.util.Objects;

record ExecutableLiteral(RuntimeValue precomputed) implements ExecutableNode {

    ExecutableLiteral {
        Objects.requireNonNull(precomputed, "precomputed must not be null");
    }
}
