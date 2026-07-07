package com.runestone.expeval_mk3.internal.ast;

import java.util.List;
import java.util.Objects;

public record SliceSubscriptSource(List<SubscriptIntegerLiteral> indexes) implements SubscriptSource {

    public SliceSubscriptSource {
        Objects.requireNonNull(indexes, "indexes");
        indexes = List.copyOf(indexes);
    }
}
