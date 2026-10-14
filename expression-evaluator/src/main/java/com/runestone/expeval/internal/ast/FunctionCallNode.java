package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.SourceSpan;

import java.util.List;
import java.util.Objects;

public record FunctionCallNode(
        NodeId id,
        SourceSpan sourceSpan,
        FunctionName name,
        List<CallArgument> arguments) implements ExpressionNode {

    public FunctionCallNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(name, "name");
        arguments = List.copyOf(arguments);
    }
}
