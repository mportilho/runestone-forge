package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.List;
import java.util.Objects;

public record NavigationChainNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExpressionNode receiver,
        List<NavigationLink> links) implements ExpressionNode {

    public NavigationChainNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        links = List.copyOf(links);
        if (links.isEmpty()) {
            throw new IllegalArgumentException("Navigation chain must contain at least one link");
        }
    }
}
