package com.runestone.expeval_mk3.internal.ast;

import java.math.BigInteger;
import java.util.Objects;

record BigIntegerLiteralValue(BigInteger value) implements LiteralValue {

    BigIntegerLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
