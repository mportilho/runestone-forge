package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record LambdaCallArgument(LambdaNode lambda) implements CallArgument {

    LambdaCallArgument {
        Objects.requireNonNull(lambda, "lambda");
    }
}
