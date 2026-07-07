package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

public record AssignedSymbolSource(NodeId nodeId, String name, SourceSpan sourceSpan) {

    public AssignedSymbolSource {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    static List<AssignedSymbolSource> from(AssignmentTargetNode target) {
        return switch (target) {
            case IdentifierAssignmentTargetNode identifier -> List.of(new AssignedSymbolSource(
                    identifier.id(),
                    identifier.name(),
                    identifier.sourceSpan()));
            case DestructuringAssignmentTargetNode destructuring -> destructuring.elements().stream()
                    .map(element -> new AssignedSymbolSource(element.id(), element.name(), element.sourceSpan()))
                    .toList();
        };
    }
}
