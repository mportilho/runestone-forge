package com.runestone.expeval.internal.ast;

import java.util.List;

public record ExpressionFileNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    List<AssignmentNode> assignments,
    ExpressionNode resultExpression
) implements Node {

    public ExpressionFileNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        assignments = AstValidation.copyList(assignments, "assignments");
        // resultExpression is null for assignment-only mode (AssignmentExpression)
    }
}
