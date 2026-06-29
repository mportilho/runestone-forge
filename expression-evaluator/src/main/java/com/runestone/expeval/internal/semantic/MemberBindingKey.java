package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.internal.ast.NodeId;

import java.util.Objects;

public record MemberBindingKey(NodeId propertyChainNodeId, int accessIndex) {

    public MemberBindingKey {
        Objects.requireNonNull(propertyChainNodeId, "propertyChainNodeId must not be null");
        if (accessIndex < 0) {
            throw new IllegalArgumentException("accessIndex must not be negative");
        }
    }
}
