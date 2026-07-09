package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record WildcardNavigationLink(NodeId id, SourceSpan sourceSpan, WildcardNavigationKind kind, boolean safe)
        implements NavigationLink {

    WildcardNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(kind, "kind");
        if (kind == WildcardNavigationKind.CHILD && safe) {
            throw new IllegalArgumentException("Child wildcard navigation cannot be safe");
        }
    }
}
