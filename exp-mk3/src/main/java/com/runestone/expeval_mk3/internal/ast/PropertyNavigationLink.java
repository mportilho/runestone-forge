package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.Objects;

record PropertyNavigationLink(NodeId id, SourceSpan sourceSpan, MemberName memberName, boolean safeNavigation)
        implements NavigationLink {

    PropertyNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(memberName, "memberName");
    }
}
