package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

public record IntegerSubscriptSliceBound(SubscriptIntegerLiteral integer) implements SubscriptSliceBound {

    public IntegerSubscriptSliceBound {
        Objects.requireNonNull(integer, "integer");
    }
}
