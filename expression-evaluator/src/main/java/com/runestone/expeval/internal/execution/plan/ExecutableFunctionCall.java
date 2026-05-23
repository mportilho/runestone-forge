package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.internal.semantic.ResolvedFunctionBinding;

import java.util.List;
import java.util.Objects;

public record ExecutableFunctionCall(
        ResolvedFunctionBinding binding,
        List<ExecutableNode> arguments,
        Object[] foldedArgs,
        Object foldedResult
) implements ExecutableNode {

    public ExecutableFunctionCall {
        Objects.requireNonNull(binding, "binding must not be null");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments must not be null"));
    }

    public static ExecutableFunctionCall of(ResolvedFunctionBinding binding, List<ExecutableNode> arguments) {
        return new ExecutableFunctionCall(binding, arguments, null, null);
    }

    public static ExecutableFunctionCall folded(ResolvedFunctionBinding binding, List<ExecutableNode> arguments,
                                         Object[] foldedArgs, Object foldedResult) {
        return new ExecutableFunctionCall(binding, arguments, foldedArgs, foldedResult);
    }

    public boolean isFolded() {
        return foldedResult != null;
    }
}
