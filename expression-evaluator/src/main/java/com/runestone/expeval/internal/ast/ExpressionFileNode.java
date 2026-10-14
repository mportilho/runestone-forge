package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ExpressionFileNode(
        NodeId id,
        SourceSpan sourceSpan,
        List<AssignmentNode> assignments,
        Optional<ExpressionNode> resultExpression) implements AstNode {

    public ExpressionFileNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(assignments, "assignments");
        assignments = List.copyOf(assignments);
        Objects.requireNonNull(resultExpression, "resultExpression");
    }
}
