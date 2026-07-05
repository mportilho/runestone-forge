package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

record StringLiteralValue(String value) implements LiteralValue {

    StringLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
