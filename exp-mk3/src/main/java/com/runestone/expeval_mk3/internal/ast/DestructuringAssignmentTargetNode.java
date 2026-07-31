package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.List;
import java.util.Objects;

public record DestructuringAssignmentTargetNode(
        NodeId id,
        SourceSpan sourceSpan,
        List<IdentifierAssignmentTargetNode> elements) implements AssignmentTargetNode {

    public DestructuringAssignmentTargetNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(elements, "elements");
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("elements must not be empty");
        }
        elements = List.copyOf(elements);
    }
}
