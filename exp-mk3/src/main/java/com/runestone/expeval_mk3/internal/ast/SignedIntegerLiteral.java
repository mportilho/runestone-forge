package com.runestone.expeval_mk3.internal.ast;

import java.math.BigInteger;
import java.util.Objects;

record SignedIntegerLiteral(BigInteger value, IntegerLiteralFormat format) {

    SignedIntegerLiteral {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(format, "format");
    }
}
