package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

record NavigationChainNode(NodeId id, SourceSpan sourceSpan, ExpressionNode receiver, List<NavigationLink> links)
        implements ExpressionNode {

    NavigationChainNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(receiver, "receiver");
        links = List.copyOf(links);
        if (links.isEmpty()) {
            throw new IllegalArgumentException("links must not be empty");
        }
    }
}
