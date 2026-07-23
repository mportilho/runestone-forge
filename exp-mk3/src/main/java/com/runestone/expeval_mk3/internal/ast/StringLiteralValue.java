package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

public record StringLiteralValue(String value) implements LiteralValue {

    public StringLiteralValue {
        Objects.requireNonNull(value, "value");
    }
}
