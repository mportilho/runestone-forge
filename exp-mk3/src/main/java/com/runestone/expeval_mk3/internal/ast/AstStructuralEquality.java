package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

final class AstStructuralEquality {

    private AstStructuralEquality() {
    }

    static boolean equals(ExpressionFileNode left, ExpressionFileNode right) {
        Objects.requireNonNull(left, "left");
        Objects.requireNonNull(right, "right");

        if (left.assignments().size() != right.assignments().size()) {
            return false;
        }
        for (int index = 0; index < left.assignments().size(); index++) {
            if (!equals(left.assignments().get(index), right.assignments().get(index))) {
                return false;
            }
        }
        if (left.resultExpression().isEmpty() || right.resultExpression().isEmpty()) {
            return left.resultExpression().isEmpty() && right.resultExpression().isEmpty();
        }
        return equals(left.resultExpression().orElseThrow(), right.resultExpression().orElseThrow());
    }

    private static boolean equals(AssignmentNode left, AssignmentNode right) {
        return equals(left.target(), right.target()) && equals(left.expression(), right.expression());
    }

    private static boolean equals(AssignmentTargetNode left, AssignmentTargetNode right) {
        if (left instanceof IdentifierAssignmentTargetNode leftIdentifier
                && right instanceof IdentifierAssignmentTargetNode rightIdentifier) {
            return leftIdentifier.name().equals(rightIdentifier.name());
        }
        return false;
    }

    private static boolean equals(ExpressionNode left, ExpressionNode right) {
        return switch (left) {
            case IdentifierNode leftIdentifier when right instanceof IdentifierNode rightIdentifier ->
                    leftIdentifier.name().equals(rightIdentifier.name());
            case LiteralNode leftLiteral when right instanceof LiteralNode rightLiteral ->
                    leftLiteral.value().equals(rightLiteral.value());
            default -> false;
        };
    }
}
