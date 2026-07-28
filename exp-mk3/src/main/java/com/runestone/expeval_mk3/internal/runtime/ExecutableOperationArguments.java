package com.runestone.expeval_mk3.internal.runtime;

import java.util.List;
import java.util.Objects;

public record ExecutableOperationArguments(
        List<ExecutableNode> valueArguments,
        List<ExecutableLambda> lambdaArguments) {

    public ExecutableOperationArguments {
        valueArguments = List.copyOf(Objects.requireNonNull(valueArguments, "valueArguments"));
        lambdaArguments = List.copyOf(Objects.requireNonNull(lambdaArguments, "lambdaArguments"));
    }
}
