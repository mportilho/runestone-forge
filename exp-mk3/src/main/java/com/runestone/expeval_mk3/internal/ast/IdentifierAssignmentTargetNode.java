package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.SourceSpan;
import java.util.Objects;

public record IdentifierAssignmentTargetNode(NodeId id, SourceSpan sourceSpan, String name) implements AssignmentTargetNode {

    public IdentifierAssignmentTargetNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(name, "name");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }
}
