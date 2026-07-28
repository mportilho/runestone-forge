package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

public record LambdaCallArgument(LambdaNode lambda) implements CallArgument {

    public LambdaCallArgument {
        Objects.requireNonNull(lambda, "lambda");
    }
}
