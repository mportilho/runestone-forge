package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

public record IndexSubscriptSource(SubscriptIntegerLiteral index) implements SubscriptSource {

    public IndexSubscriptSource {
        Objects.requireNonNull(index, "index");
    }
}
