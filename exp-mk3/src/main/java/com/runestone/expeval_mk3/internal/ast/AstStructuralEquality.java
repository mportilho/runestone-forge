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
                    leftConditional.sourceForm() == rightConditional.sourceForm()
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
                            && equals(leftMembership.value(), rightMembership.value())
                            && equals(leftMembership.candidates(), rightMembership.candidates());
            case NullCoalescenceNode leftNullCoalescence
                    when right instanceof NullCoalescenceNode rightNullCoalescence ->
                    equalsExpressionLists(leftNullCoalescence.operands(), rightNullCoalescence.operands());
            case PostfixOperationNode leftPostfix when right instanceof PostfixOperationNode rightPostfix ->
                    equals(leftPostfix.operand(), rightPostfix.operand())
                            && equalsPostfixOperators(leftPostfix, rightPostfix);
            case UnaryOperationNode leftUnary when right instanceof UnaryOperationNode rightUnary ->
                    leftUnary.operator() == rightUnary.operator() && equals(leftUnary.operand(), rightUnary.operand());
            case VectorLiteralNode leftVector when right instanceof VectorLiteralNode rightVector ->
                    equalsExpressionLists(leftVector.elements(), rightVector.elements());
            default -> false;
        };
    }

    private static boolean equalsBranches(List<ConditionalBranchNode> left, List<ConditionalBranchNode> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            if (!equals(left.get(index).condition(), right.get(index).condition())
                    || !equals(left.get(index).resultExpression(), right.get(index).resultExpression())) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsExpressionLists(List<ExpressionNode> left, List<ExpressionNode> right) {
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

    private static boolean equalsPostfixOperators(PostfixOperationNode left, PostfixOperationNode right) {
        if (left.operators().size() != right.operators().size()) {
            return false;
        }
        for (int index = 0; index < left.operators().size(); index++) {
            if (left.operators().get(index).operator() != right.operators().get(index).operator()) {
                return false;
            }
        }
        return true;
    }
}
