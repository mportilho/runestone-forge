package com.runestone.expeval2.internal.runtime;

import java.util.List;
import java.util.Objects;

record ExecutableVectorLiteral(List<ExecutableNode> elements) implements ExecutableNode {

    ExecutableVectorLiteral {
        elements = List.copyOf(Objects.requireNonNull(elements, "elements must not be null"));
    }
}
