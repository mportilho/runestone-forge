package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record CurrentItemSource(NodeId nodeId, int depth, SourceSpan sourceSpan) {

    public CurrentItemSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        if (depth < 0) {
            throw new IllegalArgumentException("depth must not be negative");
        }
    }
}
