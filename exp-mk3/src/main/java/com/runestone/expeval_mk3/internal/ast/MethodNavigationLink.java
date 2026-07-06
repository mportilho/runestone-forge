package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

record MethodNavigationLink(
        NodeId id,
        SourceSpan sourceSpan,
        MemberName memberName,
        boolean safeNavigation,
        List<ExpressionNode> arguments) implements NavigationLink {

    MethodNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(memberName, "memberName");
        arguments = List.copyOf(arguments);
    }
}
