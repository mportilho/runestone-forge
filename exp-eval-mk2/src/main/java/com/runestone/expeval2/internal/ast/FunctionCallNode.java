package com.runestone.expeval2.internal.ast;

import java.util.List;

public record FunctionCallNode(
    NodeId nodeId,
    SourceSpan sourceSpan,
    String functionName,
    List<ExpressionNode> arguments
) implements ExpressionNode {

    public FunctionCallNode {
        AstValidation.requireNodeId(nodeId);
        AstValidation.requireSourceSpan(sourceSpan);
        AstValidation.requireNonBlank(functionName, "functionName");
        arguments = AstValidation.copyList(arguments, "arguments");
    }
}
