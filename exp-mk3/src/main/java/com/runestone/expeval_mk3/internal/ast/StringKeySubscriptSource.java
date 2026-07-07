package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

public record StringKeySubscriptSource(String key) implements SubscriptSource {

    public StringKeySubscriptSource {
        key = Objects.requireNonNull(key, "key");
    }
}
