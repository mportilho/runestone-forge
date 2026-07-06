package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

record CollectionOperationNavigationLink(
        NodeId id,
        SourceSpan sourceSpan,
        MemberName operationName,
        List<CollectionOperationArgument> arguments) implements NavigationLink {

    CollectionOperationNavigationLink {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(operationName, "operationName");
        arguments = List.copyOf(arguments);
    }

    @Override
    public boolean safeNavigation() {
        return false;
    }
}
