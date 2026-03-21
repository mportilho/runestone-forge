package com.runestone.expeval2.internal.runtime;

import java.util.List;
import java.util.Objects;

record ExecutableFunctionCall(
        ResolvedFunctionBinding binding,
        List<ExecutableNode> arguments,
        Object[] foldedArgs,
        Object foldedResult
) implements ExecutableNode {

    ExecutableFunctionCall {
        Objects.requireNonNull(binding, "binding must not be null");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
    }

    static ExecutableFunctionCall of(ResolvedFunctionBinding binding, List<ExecutableNode> arguments) {
        return new ExecutableFunctionCall(binding, arguments, null, null);
    }

    static ExecutableFunctionCall folded(ResolvedFunctionBinding binding, List<ExecutableNode> arguments,
                                         Object[] foldedArgs, Object foldedResult) {
        return new ExecutableFunctionCall(binding, arguments, foldedArgs, foldedResult);
    }

    boolean isFolded() {
        return foldedResult != null;
    }
}
