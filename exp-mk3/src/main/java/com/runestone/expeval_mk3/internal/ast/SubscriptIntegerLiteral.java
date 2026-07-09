package com.runestone.expeval_mk3.internal.ast;

import java.math.BigInteger;
import java.util.Objects;

record SubscriptIntegerLiteral(BigInteger value, IntegerLiteralBase base) {

    SubscriptIntegerLiteral(long value, IntegerLiteralBase base) {
        this(BigInteger.valueOf(value), base);
    }

    SubscriptIntegerLiteral {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(base, "base");
    }
}
