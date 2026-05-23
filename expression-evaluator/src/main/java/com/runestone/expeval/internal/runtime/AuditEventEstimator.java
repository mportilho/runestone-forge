package com.runestone.expeval.internal.runtime;

import java.util.List;
import java.util.Objects;

final class AuditEventEstimator {

    private AuditEventEstimator() {
    }

    static int estimate(List<ExecutableAssignment> assignments,
                        ExecutableNode resultExpression,
                        int foldedVariableReads) {
        Objects.requireNonNull(assignments, "assignments must not be null");
        if (foldedVariableReads < 0) {
            throw new IllegalArgumentException("foldedVariableReads must be >= 0");
        }

        int count = foldedVariableReads;
        for (ExecutableAssignment assignment : assignments) {
            count += countAssignmentEvents(assignment);
        }
        if (resultExpression != null) {
            count += countNodeEvents(resultExpression);
        }
        return count;
    }

    private static int countAssignmentEvents(ExecutableAssignment assignment) {
        return switch (assignment) {
            case ExecutableSimpleAssignment simpleAssignment ->
                    1 + countNodeEvents(simpleAssignment.value());
            case ExecutableDestructuringAssignment destructuringAssignment ->
                    destructuringAssignment.targets().size() + countNodeEvents(destructuringAssignment.value());
        };
    }

    private static int countNodeEvents(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> 0;
            case ExecutableDynamicLiteral ignored -> 1;
            case ExecutableIdentifier ignored -> 1;
            case ExecutablePropertyChain chain -> countPropertyChainEvents(chain);
            case ExecutableFunctionCall functionCall -> countFunctionCallEvents(functionCall);
            case ExecutableBinaryOp binaryOperation ->
                    countNodeEvents(binaryOperation.left()) + countNodeEvents(binaryOperation.right());
            case ExecutableTernaryOp ternaryOperation ->
                    countNodeEvents(ternaryOperation.first())
                            + countNodeEvents(ternaryOperation.second())
                            + countNodeEvents(ternaryOperation.third());
            case ExecutableUnaryOp unaryOperation -> countNodeEvents(unaryOperation.operand());
            case ExecutablePostfixOp postfixOperation -> countNodeEvents(postfixOperation.operand());
            case ExecutableConditional conditional -> countConditionalEvents(conditional);
            case ExecutableSimpleConditional simpleConditional -> countSimpleConditionalEvents(simpleConditional);
            case ExecutableVectorLiteral vectorLiteral -> countVectorEvents(vectorLiteral);
            case ExecutableNullCoalesce nullCoalesce ->
                    countNodeEvents(nullCoalesce.left()) + countNodeEvents(nullCoalesce.right());
            case ExecutableRegexOp regexOperation -> countNodeEvents(regexOperation.subject());
        };
    }

    private static int countFunctionCallEvents(ExecutableFunctionCall functionCall) {
        int count = 1;
        for (ExecutableNode argument : functionCall.arguments()) {
            count += countNodeEvents(argument);
        }
        return count;
    }

    private static int countConditionalEvents(ExecutableConditional conditional) {
        int conditionCost = 0;
        for (ExecutableNode condition : conditional.conditions()) {
            conditionCost += countNodeEvents(condition);
        }

        int maxBranchCost = countNodeEvents(conditional.elseExpression());
        for (ExecutableNode result : conditional.results()) {
            maxBranchCost = Math.max(maxBranchCost, countNodeEvents(result));
        }
        return conditionCost + maxBranchCost;
    }

    private static int countSimpleConditionalEvents(ExecutableSimpleConditional simpleConditional) {
        int conditionCost = countNodeEvents(simpleConditional.condition());
        int maxBranchCost = Math.max(
                countNodeEvents(simpleConditional.thenExpression()),
                countNodeEvents(simpleConditional.elseExpression()));
        return conditionCost + maxBranchCost;
    }

    private static int countVectorEvents(ExecutableVectorLiteral vectorLiteral) {
        int count = 0;
        for (ExecutableNode element : vectorLiteral.elements()) {
            count += countNodeEvents(element);
        }
        return count;
    }

    private static int countPropertyChainEvents(ExecutablePropertyChain chain) {
        int count = countNodeEvents(chain.root());
        for (ExecutablePropertyChain.ExecutableAccess access : chain.chain()) {
            if (access instanceof ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke) {
                for (ExecutableNode argument : methodInvoke.arguments()) {
                    count += countNodeEvents(argument);
                }
                continue;
            }
            if (access instanceof ExecutablePropertyChain.ReflectiveMethodInvoke reflectiveMethodInvoke) {
                for (ExecutableNode argument : reflectiveMethodInvoke.arguments()) {
                    count += countNodeEvents(argument);
                }
            }
        }
        return count;
    }
}
