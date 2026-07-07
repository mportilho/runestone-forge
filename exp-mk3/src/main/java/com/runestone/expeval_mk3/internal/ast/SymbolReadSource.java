package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record SymbolReadSource(NodeId nodeId, String name, SourceSpan sourceSpan) {

    public SymbolReadSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
