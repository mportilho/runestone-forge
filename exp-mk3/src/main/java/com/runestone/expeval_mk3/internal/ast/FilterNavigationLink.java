package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Objects;

public record FilterNavigationLink(NodeId id, SourceSpan sourceSpan, ExpressionNode predicate, boolean safe)
        implements NavigationLink {

    public FilterNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(predicate, "predicate");
    }
}
