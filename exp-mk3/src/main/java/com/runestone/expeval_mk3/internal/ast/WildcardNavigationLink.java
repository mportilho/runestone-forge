package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;
import java.util.Objects;

record WildcardNavigationLink(NodeId id, SourceSpan sourceSpan) implements NavigationLink {

    WildcardNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    @Override
    public boolean safeNavigation() {
        return false;
    }
}
