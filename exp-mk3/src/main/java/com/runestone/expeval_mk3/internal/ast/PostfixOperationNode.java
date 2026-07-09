package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

record PostfixOperationNode(
        NodeId id,
        SourceSpan sourceSpan,
        ExpressionNode operand,
        List<PostfixOperatorOccurrence> operations) implements ExpressionNode {

    PostfixOperationNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(operand, "operand");
        Objects.requireNonNull(operations, "operations");
        operations = List.copyOf(operations);
        if (operations.isEmpty()) {
            throw new IllegalArgumentException("postfix operations must not be empty");
        }
    }
}
