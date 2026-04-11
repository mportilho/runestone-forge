package com.runestone.expeval.internal.runtime;

import java.util.Objects;

record ExecutableNullCoalesce(
        ExecutableNode left,
        ExecutableNode right
) implements ExecutableNode {

    ExecutableNullCoalesce {
        Objects.requireNonNull(left, "left must not be null");
        Objects.requireNonNull(right, "right must not be null");
    }
}
