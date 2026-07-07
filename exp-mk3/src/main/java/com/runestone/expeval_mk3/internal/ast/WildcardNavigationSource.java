package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record WildcardNavigationSource(NodeId nodeId, SourceSpan sourceSpan) implements NavigationLinkSource {

    public WildcardNavigationSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
