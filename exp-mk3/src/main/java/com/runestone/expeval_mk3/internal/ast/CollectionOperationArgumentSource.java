package com.runestone.expeval_mk3.internal.ast;

import java.util.List;
import java.util.Objects;

public record CollectionOperationArgumentSource(
        NodeId nodeId,
        CollectionOperationArgumentKind kind,
        int currentItemDepth,
        List<NodeId> currentItemNodeIds) {

    public CollectionOperationArgumentSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(currentItemNodeIds, "currentItemNodeIds");
        currentItemNodeIds = List.copyOf(currentItemNodeIds);
        if (currentItemDepth < 0) {
            throw new IllegalArgumentException("currentItemDepth must not be negative");
        }
        if (kind == CollectionOperationArgumentKind.LAMBDA && currentItemDepth < 1) {
            throw new IllegalArgumentException("lambda currentItemDepth must be one or greater");
        }
    }
}
