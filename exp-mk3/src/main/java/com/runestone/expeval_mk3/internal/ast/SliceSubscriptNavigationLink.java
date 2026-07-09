package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record SliceSubscriptNavigationLink(
        NodeId id,
        SourceSpan sourceSpan,
        SubscriptSliceBound start,
        SubscriptSliceBound end,
        boolean safe) implements NavigationLink {

    SliceSubscriptNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
    }
}
