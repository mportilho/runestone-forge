package com.runestone.expeval_mk3.internal.ast;

import java.util.List;
import java.util.Objects;

public record CurrentItemFlow(List<CurrentItemSource> sources, int maxDepth) {

    public CurrentItemFlow {
        Objects.requireNonNull(sources, "sources");
        sources = List.copyOf(sources);
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must not be negative");
        }
    }
}
