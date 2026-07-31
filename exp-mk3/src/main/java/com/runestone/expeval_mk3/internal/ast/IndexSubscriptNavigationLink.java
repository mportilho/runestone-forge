package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Objects;

public record IndexSubscriptNavigationLink(NodeId id, SourceSpan sourceSpan, SubscriptIntegerLiteral index, boolean safe)
        implements NavigationLink {

    public IndexSubscriptNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(index, "index");
    }
}
