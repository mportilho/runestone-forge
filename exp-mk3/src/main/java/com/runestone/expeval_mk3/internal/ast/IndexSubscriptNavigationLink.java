package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record IndexSubscriptNavigationLink(NodeId id, SourceSpan sourceSpan, SubscriptIntegerLiteral index, boolean safe)
        implements NavigationLink {

    IndexSubscriptNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(index, "index");
    }
}
