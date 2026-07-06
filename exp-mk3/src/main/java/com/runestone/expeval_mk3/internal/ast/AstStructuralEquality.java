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
                    equalsDestructuringTargets(leftDestructuring, rightDestructuring);
            case IdentifierAssignmentTargetNode leftIdentifier
                    when right instanceof IdentifierAssignmentTargetNode rightIdentifier ->
                    leftIdentifier.name().equals(rightIdentifier.name());
            default -> false;
        };
    }

    private static boolean equalsDestructuringTargets(
            DestructuringAssignmentTargetNode left,
            DestructuringAssignmentTargetNode right) {
        if (left.elements().size() != right.elements().size()) {
            return false;
        }
        for (int index = 0; index < left.elements().size(); index++) {
            if (!left.elements().get(index).name().equals(right.elements().get(index).name())) {
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
                    leftConditional.sourceForm() == rightConditional.sourceForm()
                            && equalsBranches(leftConditional.branches(), rightConditional.branches())
                            && equals(leftConditional.elseExpression(), rightConditional.elseExpression());
            case CurrentItemNode ignored when right instanceof CurrentItemNode -> true;
            case CurrentTemporalValueNode leftCurrentTemporalValue
                    when right instanceof CurrentTemporalValueNode rightCurrentTemporalValue ->
                    leftCurrentTemporalValue.kind() == rightCurrentTemporalValue.kind();
            case FunctionCallNode leftFunctionCall when right instanceof FunctionCallNode rightFunctionCall ->
                    leftFunctionCall.name().value().equals(rightFunctionCall.name().value())
                            && equalsExpressionLists(leftFunctionCall.arguments(), rightFunctionCall.arguments());
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
            case NavigationChainNode leftNavigationChain when right instanceof NavigationChainNode rightNavigationChain ->
                    equals(leftNavigationChain.receiver(), rightNavigationChain.receiver())
                            && equalsNavigationLinks(leftNavigationChain.links(), rightNavigationChain.links());
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
            case CollectionOperationNavigationLink leftCollectionOperation
                    when right instanceof CollectionOperationNavigationLink rightCollectionOperation ->
                    leftCollectionOperation.operationName().value().equals(rightCollectionOperation.operationName().value())
                            && equalsCollectionOperationArguments(
                                    leftCollectionOperation.arguments(),
                                    rightCollectionOperation.arguments());
            case FilterNavigationLink leftFilter when right instanceof FilterNavigationLink rightFilter ->
                    leftFilter.safeNavigation() == rightFilter.safeNavigation()
                            && equals(leftFilter.predicate(), rightFilter.predicate());
            case MethodNavigationLink leftMethod when right instanceof MethodNavigationLink rightMethod ->
                    leftMethod.safeNavigation() == rightMethod.safeNavigation()
                            && leftMethod.memberName().value().equals(rightMethod.memberName().value())
                            && equalsExpressionLists(leftMethod.arguments(), rightMethod.arguments());
            case PropertyNavigationLink leftProperty when right instanceof PropertyNavigationLink rightProperty ->
                    leftProperty.safeNavigation() == rightProperty.safeNavigation()
                            && leftProperty.memberName().value().equals(rightProperty.memberName().value());
            case SubscriptNavigationLink leftSubscript when right instanceof SubscriptNavigationLink rightSubscript ->
                    leftSubscript.safeNavigation() == rightSubscript.safeNavigation()
                            && leftSubscript.subscript().equals(rightSubscript.subscript());
            case WildcardNavigationLink ignored when right instanceof WildcardNavigationLink -> true;
            default -> false;
        };
    }

    private static boolean equalsCollectionOperationArguments(
            List<CollectionOperationArgument> left,
            List<CollectionOperationArgument> right) {
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

    private static boolean equals(CollectionOperationArgument left, CollectionOperationArgument right) {
        return switch (left) {
            case LambdaCollectionOperationArgument leftLambda
                    when right instanceof LambdaCollectionOperationArgument rightLambda ->
                    equals(leftLambda.lambda().body(), rightLambda.lambda().body());
            case PositionalCollectionOperationArgument leftPositional
                    when right instanceof PositionalCollectionOperationArgument rightPositional ->
                    equals(leftPositional.expression(), rightPositional.expression());
            default -> false;
        };
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
