package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.Objects;

public record WildcardNavigationLink(NodeId id, SourceSpan sourceSpan, boolean safe)
        implements NavigationLink {

    public WildcardNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }
}
