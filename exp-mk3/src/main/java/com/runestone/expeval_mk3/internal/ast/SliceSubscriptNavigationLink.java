package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Objects;

public record SliceSubscriptNavigationLink(
        NodeId id,
        SourceSpan sourceSpan,
        SubscriptSliceBound start,
        SubscriptSliceBound end,
        boolean safe) implements NavigationLink {

    public SliceSubscriptNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
    }
}
