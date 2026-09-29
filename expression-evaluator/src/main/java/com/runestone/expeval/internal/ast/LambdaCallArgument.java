package com.runestone.expeval.internal.ast;

import java.util.Objects;

public record LambdaCallArgument(LambdaNode lambda) implements CallArgument {

    public LambdaCallArgument {
        Objects.requireNonNull(lambda, "lambda");
    }
}
