package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record RegexLeftOperandRestriction(NodeId nodeId, SourceSpan sourceSpan) {

    public RegexLeftOperandRestriction {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
