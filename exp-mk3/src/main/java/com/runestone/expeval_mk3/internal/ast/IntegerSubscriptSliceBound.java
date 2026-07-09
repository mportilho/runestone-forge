package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record IntegerSubscriptSliceBound(SubscriptIntegerLiteral integer) implements SubscriptSliceBound {

    IntegerSubscriptSliceBound {
        Objects.requireNonNull(integer, "integer");
    }
}
