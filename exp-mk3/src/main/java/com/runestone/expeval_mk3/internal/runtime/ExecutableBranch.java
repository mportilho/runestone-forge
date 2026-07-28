package com.runestone.expeval_mk3.internal.runtime;

import java.util.Objects;

public record ExecutableBranch(ExecutableNode condition, ExecutableNode consequence) {

    public ExecutableBranch {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(consequence, "consequence");
    }
}
