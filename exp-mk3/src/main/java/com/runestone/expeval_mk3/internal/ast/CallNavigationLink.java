package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

record CallNavigationLink(
        NodeId id,
        SourceSpan sourceSpan,
        MemberName memberName,
        boolean safe,
        List<CallArgument> arguments) implements NavigationLink {

    CallNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(memberName, "memberName");
        arguments = List.copyOf(arguments);
    }
}
