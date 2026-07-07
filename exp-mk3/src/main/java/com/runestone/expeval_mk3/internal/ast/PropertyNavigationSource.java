package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record PropertyNavigationSource(NodeId nodeId, SourceSpan sourceSpan, String memberName, NavigationSafety safety)
        implements NavigationLinkSource {

    public PropertyNavigationSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        memberName = Objects.requireNonNull(memberName, "memberName");
        if (memberName.isBlank()) {
            throw new IllegalArgumentException("memberName must not be blank");
        }
        Objects.requireNonNull(safety, "safety");
    }
}
