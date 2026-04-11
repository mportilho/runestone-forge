package com.runestone.expeval.internal.ast;

public record SimpleAssignmentNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    String targetName,
    ExpressionNode value
) implements AssignmentNode {

    public SimpleAssignmentNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        AstValidation.requireNonBlank(targetName, "targetName");
        AstValidation.requireNonNull(value, "value");
    }
}
