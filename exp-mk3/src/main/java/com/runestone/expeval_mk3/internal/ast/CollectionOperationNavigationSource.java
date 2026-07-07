package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

public record CollectionOperationNavigationSource(
        NodeId nodeId,
        SourceSpan sourceSpan,
        String operationName,
        List<CollectionOperationArgumentSource> arguments) implements NavigationLinkSource {

    public CollectionOperationNavigationSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        operationName = Objects.requireNonNull(operationName, "operationName");
        if (operationName.isBlank()) {
            throw new IllegalArgumentException("operationName must not be blank");
        }
        Objects.requireNonNull(arguments, "arguments");
        arguments = List.copyOf(arguments);
    }
}
