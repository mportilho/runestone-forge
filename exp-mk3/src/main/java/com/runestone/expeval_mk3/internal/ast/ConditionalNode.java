package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

record ConditionalNode(
        NodeId id,
        SourceSpan sourceSpan,
        ConditionalSourceForm sourceForm,
        List<ConditionalBranchNode> branches,
        ExpressionNode elseExpression) implements ExpressionNode {

    ConditionalNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(sourceForm, "sourceForm");
        Objects.requireNonNull(branches, "branches");
        Objects.requireNonNull(elseExpression, "elseExpression");
        branches = List.copyOf(branches);
        if (branches.isEmpty()) {
            throw new IllegalArgumentException("branches must contain at least one conditional branch");
        }
    }
}
