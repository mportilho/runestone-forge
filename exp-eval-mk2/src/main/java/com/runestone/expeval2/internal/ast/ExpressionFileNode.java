package com.runestone.expeval2.internal.ast;

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
        AstValidation.requireNonNull(resultExpression, "resultExpression");
    }
}
