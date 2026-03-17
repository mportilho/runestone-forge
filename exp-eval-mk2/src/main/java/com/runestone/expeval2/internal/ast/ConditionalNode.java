package com.runestone.expeval2.internal.ast;

import java.util.List;

public record ConditionalNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    List<ExpressionNode> conditions,
    List<ExpressionNode> results,
    ExpressionNode elseExpression
) implements ExpressionNode {

    public ConditionalNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        conditions = AstValidation.copyList(conditions, "conditions");
        results = AstValidation.copyList(results, "results");
        if (conditions.isEmpty()) {
            throw new IllegalArgumentException("conditions must not be empty");
        }
        if (conditions.size() != results.size()) {
            throw new IllegalArgumentException("conditions and results must have the same size");
        }
        AstValidation.requireNonNull(elseExpression, "elseExpression");
    }
}
