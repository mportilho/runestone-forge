package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

public record TypeJoinRestriction(NodeId resultNodeId, List<NodeId> branchNodeIds, SourceSpan sourceSpan) {

    public TypeJoinRestriction {
        Objects.requireNonNull(resultNodeId, "resultNodeId");
        Objects.requireNonNull(branchNodeIds, "branchNodeIds");
        branchNodeIds = List.copyOf(branchNodeIds);
        if (branchNodeIds.isEmpty()) {
            throw new IllegalArgumentException("branchNodeIds must not be empty");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
