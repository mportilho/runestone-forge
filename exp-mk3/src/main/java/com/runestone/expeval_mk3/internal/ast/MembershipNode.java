package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;
import java.util.Objects;

record MembershipNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExpressionNode value,
        SourceSpan operatorSpan,
        boolean negated,
        ExpressionNode candidates) implements ExpressionNode {

    MembershipNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(operatorSpan, "operatorSpan");
        Objects.requireNonNull(candidates, "candidates");
    }
}
