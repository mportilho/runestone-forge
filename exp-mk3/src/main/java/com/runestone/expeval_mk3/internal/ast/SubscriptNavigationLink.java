package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record SubscriptNavigationLink(NodeId id, SourceSpan sourceSpan, Subscript subscript, boolean safeNavigation)
        implements NavigationLink {

    SubscriptNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(subscript, "subscript");
    }
}
