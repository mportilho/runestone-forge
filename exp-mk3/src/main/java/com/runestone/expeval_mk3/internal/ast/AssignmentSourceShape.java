package com.runestone.expeval_mk3.internal.ast;

import java.util.List;
import java.util.Objects;

public record AssignmentSourceShape(int arity, List<NodeId> elementNodeIds) {

    public AssignmentSourceShape {
        if (arity < 0) {
            throw new IllegalArgumentException("arity must not be negative");
        }
        Objects.requireNonNull(elementNodeIds, "elementNodeIds");
        elementNodeIds = List.copyOf(elementNodeIds);
        if (!elementNodeIds.isEmpty() && elementNodeIds.size() != arity) {
            throw new IllegalArgumentException("element node id count must match arity when present");
        }
    }

    public static AssignmentSourceShape vectorLiteral(List<NodeId> elementNodeIds) {
        Objects.requireNonNull(elementNodeIds, "elementNodeIds");
        return new AssignmentSourceShape(elementNodeIds.size(), elementNodeIds);
    }

    public boolean hasElementNodeIds() {
        return !elementNodeIds.isEmpty();
    }
}
