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
        return switch (left) {
            case DestructuringAssignmentTargetNode leftDestructuring
                    when right instanceof DestructuringAssignmentTargetNode rightDestructuring ->
                    equalsTargets(leftDestructuring.elements(), rightDestructuring.elements());
            case IdentifierAssignmentTargetNode leftIdentifier
                    when right instanceof IdentifierAssignmentTargetNode rightIdentifier ->
                    leftIdentifier.name().equals(rightIdentifier.name());
            default -> false;
        };
    }

    private static boolean equalsTargets(
            List<IdentifierAssignmentTargetNode> left,
            List<IdentifierAssignmentTargetNode> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int index = 0; index < left.size(); index++) {
            if (!left.get(index).name().equals(right.get(index).name())) {
                return false;
            }
        }
        return true;
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
            case CurrentItemNode ignored when right instanceof CurrentItemNode -> true;
            case CurrentTemporalValueNode leftCurrentTemporalValue
                    when right instanceof CurrentTemporalValueNode rightCurrentTemporalValue ->
                    leftCurrentTemporalValue.kind() == rightCurrentTemporalValue.kind();
            case FunctionCallNode leftFunctionCall when right instanceof FunctionCallNode rightFunctionCall ->
                    leftFunctionCall.name().equals(rightFunctionCall.name())
                            && equalsCallArguments(leftFunctionCall.arguments(), rightFunctionCall.arguments());
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
            case NavigationChainNode leftNavigation when right instanceof NavigationChainNode rightNavigation ->
                    equals(leftNavigation.receiver(), rightNavigation.receiver())
                            && equalsNavigationLinks(leftNavigation.links(), rightNavigation.links());
            case NullCoalesceNode leftCoalesce when right instanceof NullCoalesceNode rightCoalesce ->
                    equalsExpressions(leftCoalesce.operands(), rightCoalesce.operands());
            case PostfixOperationNode leftPostfix when right instanceof PostfixOperationNode rightPostfix ->
                    leftPostfix.operations().stream().map(PostfixOperatorOccurrence::operator).toList()
                            .equals(rightPostfix.operations().stream().map(PostfixOperatorOccurrence::operator).toList())
                            && equals(leftPostfix.operand(), rightPostfix.operand());
            case UnaryOperationNode leftUnary when right instanceof UnaryOperationNode rightUnary ->
                    leftUnary.operator() == rightUnary.operator() && equals(leftUnary.operand(), rightUnary.operand());
            case CollectionLiteralNode leftCollection when right instanceof CollectionLiteralNode rightCollection ->
                    equalsExpressions(leftCollection.elements(), rightCollection.elements());
            default -> false;
        };
    }

    private static boolean equalsNavigationLinks(List<NavigationLink> left, List<NavigationLink> right) {
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

    private static boolean equals(NavigationLink left, NavigationLink right) {
        return switch (left) {
            case CallNavigationLink leftCall when right instanceof CallNavigationLink rightCall ->
                    leftCall.safe() == rightCall.safe()
                            && leftCall.memberName().equals(rightCall.memberName())
                            && equalsCallArguments(leftCall.arguments(), rightCall.arguments());
            case FilterNavigationLink leftFilter when right instanceof FilterNavigationLink rightFilter ->
                    leftFilter.safe() == rightFilter.safe() && equals(leftFilter.predicate(), rightFilter.predicate());
            case IndexSubscriptNavigationLink leftIndex when right instanceof IndexSubscriptNavigationLink rightIndex ->
                    leftIndex.safe() == rightIndex.safe() && leftIndex.index().equals(rightIndex.index());
            case PropertyNavigationLink leftProperty when right instanceof PropertyNavigationLink rightProperty ->
                    leftProperty.safe() == rightProperty.safe()
                            && leftProperty.memberName().equals(rightProperty.memberName());
            case SliceSubscriptNavigationLink leftSlice when right instanceof SliceSubscriptNavigationLink rightSlice ->
                    leftSlice.safe() == rightSlice.safe()
                            && leftSlice.start().equals(rightSlice.start())
                            && leftSlice.end().equals(rightSlice.end());
            case StringKeySubscriptNavigationLink leftStringKey
                    when right instanceof StringKeySubscriptNavigationLink rightStringKey ->
                    leftStringKey.safe() == rightStringKey.safe() && leftStringKey.key().equals(rightStringKey.key());
            case WildcardNavigationLink leftWildcard when right instanceof WildcardNavigationLink rightWildcard ->
                    leftWildcard.safe() == rightWildcard.safe();
            default -> false;
        };
    }

    private static boolean equalsCallArguments(List<CallArgument> left, List<CallArgument> right) {
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

    private static boolean equals(CallArgument left, CallArgument right) {
        return switch (left) {
            case ExpressionCallArgument leftExpression when right instanceof ExpressionCallArgument rightExpression ->
                    equals(leftExpression.expression(), rightExpression.expression());
            case LambdaCallArgument leftLambda when right instanceof LambdaCallArgument rightLambda ->
                    equals(leftLambda.lambda(), rightLambda.lambda());
            default -> false;
        };
    }

    private static boolean equals(LambdaNode left, LambdaNode right) {
        return equals(left.currentItem(), right.currentItem()) && equals(left.body(), right.body());
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
