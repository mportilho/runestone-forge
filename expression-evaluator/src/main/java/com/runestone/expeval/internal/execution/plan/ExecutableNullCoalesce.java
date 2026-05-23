package com.runestone.expeval.internal.execution.plan;

import java.util.Objects;

public record ExecutableNullCoalesce(
        ExecutableNode left,
        ExecutableNode right
) implements ExecutableNode {

    public ExecutableNullCoalesce {
        Objects.requireNonNull(left, "left must not be null");
        Objects.requireNonNull(right, "right must not be null");
    }
}
