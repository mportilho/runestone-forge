package com.runestone.expeval2.internal.runtime;

import java.util.List;
import java.util.Objects;

record ExecutableFunctionCall(ResolvedFunctionBinding binding, List<ExecutableNode> arguments) implements ExecutableNode {

    ExecutableFunctionCall {
        Objects.requireNonNull(binding, "binding must not be null");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
    }
}
