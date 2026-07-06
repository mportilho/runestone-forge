package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

record FunctionCallNode(NodeId id, SourceSpan sourceSpan, MemberName name, List<ExpressionNode> arguments)
        implements ExpressionNode {

    FunctionCallNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(name, "name");
        arguments = List.copyOf(arguments);
    }
}
