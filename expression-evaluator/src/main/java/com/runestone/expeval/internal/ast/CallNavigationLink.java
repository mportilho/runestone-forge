package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.List;
import java.util.Objects;

public record CallNavigationLink(
        NodeId id,
        SourceSpan sourceSpan,
        MemberName memberName,
        boolean safe,
        List<CallArgument> arguments) implements NavigationLink {

    public CallNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(memberName, "memberName");
        arguments = List.copyOf(Objects.requireNonNull(arguments, "arguments"));
    }
}
