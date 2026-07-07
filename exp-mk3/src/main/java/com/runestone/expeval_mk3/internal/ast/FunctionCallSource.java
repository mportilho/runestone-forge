package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

public record FunctionCallSource(
        NodeId nodeId,
        String name,
        List<NodeId> argumentNodeIds,
        SourceSpan sourceSpan) {

    public FunctionCallSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(argumentNodeIds, "argumentNodeIds");
        argumentNodeIds = List.copyOf(argumentNodeIds);
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
