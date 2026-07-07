package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record FilterNavigationSource(
        NodeId nodeId,
        SourceSpan sourceSpan,
        FilterNavigationPredicate predicate,
        NavigationSafety safety)
        implements NavigationLinkSource {

    public FilterNavigationSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(predicate, "predicate");
        Objects.requireNonNull(safety, "safety");
    }
}
