package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.Objects;

public record MembershipNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExpressionNode element,
        boolean negated,
        SourceSpan operatorSpan,
        ExpressionNode collection) implements ExpressionNode {

    public MembershipNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(element, "element");
        Objects.requireNonNull(operatorSpan, "operatorSpan");
        Objects.requireNonNull(collection, "collection");
    }
}
