package com.runestone.expeval_mk3.internal.ast;

import java.util.List;
import java.util.Objects;

public record FilterNavigationPredicate(NodeId nodeId, int currentItemDepth, List<NodeId> currentItemNodeIds) {

    public FilterNavigationPredicate {
        Objects.requireNonNull(nodeId, "nodeId");
        if (currentItemDepth < 1) {
            throw new IllegalArgumentException("currentItemDepth must be one or greater");
        }
        Objects.requireNonNull(currentItemNodeIds, "currentItemNodeIds");
        currentItemNodeIds = List.copyOf(currentItemNodeIds);
    }
}
