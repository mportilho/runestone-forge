package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record MembershipTypeRestriction(NodeId valueNodeId, NodeId candidatesNodeId, SourceSpan sourceSpan) {

    public MembershipTypeRestriction {
        Objects.requireNonNull(valueNodeId, "valueNodeId");
        Objects.requireNonNull(candidatesNodeId, "candidatesNodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
