package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record FilterNavigationLink(NodeId id, SourceSpan sourceSpan, ExpressionNode predicate, boolean safeNavigation)
        implements NavigationLink {

    FilterNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(predicate, "predicate");
    }
}
