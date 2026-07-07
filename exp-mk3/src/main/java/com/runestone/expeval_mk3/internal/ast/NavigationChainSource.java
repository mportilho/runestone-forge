package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

public record NavigationChainSource(
        NodeId nodeId,
        NodeId receiverNodeId,
        List<NavigationLinkSource> links,
        SourceSpan sourceSpan) {

    public NavigationChainSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(receiverNodeId, "receiverNodeId");
        Objects.requireNonNull(links, "links");
        links = List.copyOf(links);
        if (links.isEmpty()) {
            throw new IllegalArgumentException("links must not be empty");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
