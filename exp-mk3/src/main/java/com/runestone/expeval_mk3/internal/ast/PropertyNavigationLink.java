package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Objects;

public record PropertyNavigationLink(NodeId id, SourceSpan sourceSpan, MemberName memberName, boolean safe)
        implements NavigationLink {

    public PropertyNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(memberName, "memberName");
    }
}
