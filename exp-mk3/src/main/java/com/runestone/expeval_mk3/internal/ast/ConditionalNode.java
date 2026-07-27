package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;

public record ConditionalNode(
        NodeId id,
        SourceSpan sourceSpan,
        ConditionalSyntax syntax,
        List<ConditionalBranchNode> branches,
        List<ConditionalSeparatorOccurrence> separators,
        ExpressionNode elseExpression) implements ExpressionNode {

    public ConditionalNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(sourceSpan, "sourceSpan");
        Objects.requireNonNull(syntax, "syntax");
        Objects.requireNonNull(branches, "branches");
        branches = List.copyOf(branches);
        Objects.requireNonNull(separators, "separators");
        separators = List.copyOf(separators);
        Objects.requireNonNull(elseExpression, "elseExpression");
        if (branches.isEmpty()) {
            throw new IllegalArgumentException("conditional expression needs at least one branch");
        }
        int expectedSeparators = syntax == ConditionalSyntax.FUNCTIONAL ? branches.size() * 2 : 0;
        if (separators.size() != expectedSeparators) {
            throw new IllegalArgumentException("conditional separator count does not match its syntax");
        }
    }
}
