package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

public record SameTypeRestriction(List<NodeId> nodeIds, SourceSpan sourceSpan, TypeCompatibilityMode mode) {

    public SameTypeRestriction {
        Objects.requireNonNull(nodeIds, "nodeIds");
        nodeIds = List.copyOf(nodeIds);
        if (nodeIds.size() < 2) {
            throw new IllegalArgumentException("nodeIds must contain at least two entries");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(mode, "mode");
    }
}
