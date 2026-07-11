package com.runestone.expeval_mk3.internal.ast;

import java.math.BigInteger;
import java.util.Objects;

record SubscriptIntegerLiteral(BigInteger value) {

    SubscriptIntegerLiteral(long value) {
        this(BigInteger.valueOf(value));
    }

    SubscriptIntegerLiteral {
        Objects.requireNonNull(value, "value");
    }
}
