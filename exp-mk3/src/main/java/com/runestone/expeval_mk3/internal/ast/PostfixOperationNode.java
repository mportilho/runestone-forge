package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

record PostfixOperationNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExpressionNode operand,
        List<PostfixOperatorOccurrence> operators) implements ExpressionNode {

    PostfixOperationNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(operand, "operand");
        Objects.requireNonNull(operators, "operators");
        operators = List.copyOf(operators);
        if (operators.isEmpty()) {
            throw new IllegalArgumentException("operators must not be empty");
        }
    }
}
