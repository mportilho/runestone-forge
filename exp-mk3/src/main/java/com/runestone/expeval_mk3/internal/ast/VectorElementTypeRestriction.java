package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

public record VectorElementTypeRestriction(NodeId vectorNodeId, List<NodeId> elementNodeIds, SourceSpan sourceSpan) {

    public VectorElementTypeRestriction {
        Objects.requireNonNull(vectorNodeId, "vectorNodeId");
        Objects.requireNonNull(elementNodeIds, "elementNodeIds");
        elementNodeIds = List.copyOf(elementNodeIds);
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
