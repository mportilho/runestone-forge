package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

public record StringKeySubscriptNavigationLink(NodeId id, SourceSpan sourceSpan, String key, boolean safe)
        implements NavigationLink {

    public StringKeySubscriptNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(key, "key");
    }
}
