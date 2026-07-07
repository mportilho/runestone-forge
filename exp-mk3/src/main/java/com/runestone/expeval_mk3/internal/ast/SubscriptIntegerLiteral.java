package com.runestone.expeval_mk3.internal.ast;

import java.math.BigInteger;
import java.util.Objects;

public record SubscriptIntegerLiteral(BigInteger value, SubscriptIntegerFormat format) {

    public SubscriptIntegerLiteral {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(format, "format");
    }
}
