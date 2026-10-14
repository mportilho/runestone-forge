package com.runestone.expeval.internal.ast;

import java.util.Objects;

public record IntegerSubscriptSliceBound(SubscriptIntegerLiteral integer) implements SubscriptSliceBound {

    public IntegerSubscriptSliceBound {
        Objects.requireNonNull(integer, "integer");
    }
}
