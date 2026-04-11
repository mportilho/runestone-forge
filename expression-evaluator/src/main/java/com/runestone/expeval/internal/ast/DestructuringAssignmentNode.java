package com.runestone.expeval.internal.ast;

import java.util.List;

public record DestructuringAssignmentNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    List<String> targetNames,
    ExpressionNode value
) implements AssignmentNode {

    public DestructuringAssignmentNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        targetNames = AstValidation.copyList(targetNames, "targetNames");
        if (targetNames.isEmpty()) {
            throw new IllegalArgumentException("targetNames must not be empty");
        }
        if (targetNames.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("targetNames must not contain blank values");
        }
        AstValidation.requireNonNull(value, "value");
    }
}
