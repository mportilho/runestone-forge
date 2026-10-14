package com.runestone.expeval.internal.ast;

import java.math.BigInteger;
import java.util.Objects;

public record SubscriptIntegerLiteral(BigInteger value) {

    SubscriptIntegerLiteral(long value) {
        this(BigInteger.valueOf(value));
    }

    public SubscriptIntegerLiteral {
        Objects.requireNonNull(value, "value");
    }
}
