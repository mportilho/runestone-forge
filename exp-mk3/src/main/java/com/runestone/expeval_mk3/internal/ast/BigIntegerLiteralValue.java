package com.runestone.expeval_mk3.internal.ast;

import java.math.BigInteger;
import java.util.Objects;

public record BigIntegerLiteralValue(BigInteger value) implements LiteralValue {

    public BigIntegerLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
