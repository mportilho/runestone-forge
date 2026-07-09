package com.runestone.expeval_mk3.internal.ast;

import java.util.List;
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
            case BetweenNode leftBetween when right instanceof BetweenNode rightBetween ->
                    leftBetween.negated() == rightBetween.negated()
                            && equals(leftBetween.value(), rightBetween.value())
                            && equals(leftBetween.lowerBound(), rightBetween.lowerBound())
                            && equals(leftBetween.upperBound(), rightBetween.upperBound());
            case BinaryOperationNode leftBinary when right instanceof BinaryOperationNode rightBinary ->
                    leftBinary.operator() == rightBinary.operator()
                            && equals(leftBinary.left(), rightBinary.left())
                            && equals(leftBinary.right(), rightBinary.right());
            case ConditionalNode leftConditional when right instanceof ConditionalNode rightConditional ->
                    leftConditional.syntax() == rightConditional.syntax()
                            && equalsSeparators(leftConditional.separators(), rightConditional.separators())
                            && equalsBranches(leftConditional.branches(), rightConditional.branches())
                            && equals(leftConditional.elseExpression(), rightConditional.elseExpression());
            case CurrentTemporalValueNode leftCurrentTemporalValue
                    when right instanceof CurrentTemporalValueNode rightCurrentTemporalValue ->
                    leftCurrentTemporalValue.kind() == rightCurrentTemporalValue.kind();
            case GroupedExpressionNode leftGrouped when right instanceof GroupedExpressionNode rightGrouped ->
                    equals(leftGrouped.expression(), rightGrouped.expression());
            case IdentifierNode leftIdentifier when right instanceof IdentifierNode rightIdentifier ->
                    leftIdentifier.name().equals(rightIdentifier.name());
            case LiteralNode leftLiteral when right instanceof LiteralNode rightLiteral ->
                    leftLiteral.value().equals(rightLiteral.value());
            case MembershipNode leftMembership when right instanceof MembershipNode rightMembership ->
                    leftMembership.negated() == rightMembership.negated()
                            && equals(leftMembership.element(), rightMembership.element())
                            && equals(leftMembership.collection(), rightMembership.collection());
            case NullCoalesceNode leftCoalesce when right instanceof NullCoalesceNode rightCoalesce ->
                    equalsExpressions(leftCoalesce.operands(), rightCoalesce.operands());
            case PostfixOperationNode leftPostfix when right instanceof PostfixOperationNode rightPostfix ->
                    leftPostfix.operations().stream().map(PostfixOperatorOccurrence::operator).toList()
                            .equals(rightPostfix.operations().stream().map(PostfixOperatorOccurrence::operator).toList())
                            && equals(leftPostfix.operand(), rightPostfix.operand());
            case UnaryOperationNode leftUnary when right instanceof UnaryOperationNode rightUnary ->
                    leftUnary.operator() == rightUnary.operator() && equals(leftUnary.operand(), rightUnary.operand());
            case VectorLiteralNode leftVector when right instanceof VectorLiteralNode rightVector ->
                    equalsExpressions(leftVector.elements(), rightVector.elements());
            default -> false;
        };
    }

    private static boolean equalsSeparators(
            List<ConditionalSeparatorOccurrence> left,
            List<ConditionalSeparatorOccurrence> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            if (left.get(index).separator() != right.get(index).separator()) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsBranches(List<ConditionalBranchNode> left, List<ConditionalBranchNode> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            ConditionalBranchNode leftBranch = left.get(index);
            ConditionalBranchNode rightBranch = right.get(index);
            if (!equals(leftBranch.condition(), rightBranch.condition())
                    || !equals(leftBranch.consequence(), rightBranch.consequence())) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsExpressions(List<ExpressionNode> left, List<ExpressionNode> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            if (!equals(left.get(index), right.get(index))) {
                return false;
            }
        }
        return true;
    }
}
