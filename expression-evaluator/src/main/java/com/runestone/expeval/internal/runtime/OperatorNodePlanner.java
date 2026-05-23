package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.internal.ast.BinaryOperationNode;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.PostfixOperationNode;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.ast.UnaryOperationNode;

import java.math.MathContext;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;

final class OperatorNodePlanner {

    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;
    private final Function<ExpressionNode, ExecutableNode> nodeBuilder;

    OperatorNodePlanner(
            RuntimeServices runtimeServices,
            MathContext mathContext,
            Function<ExpressionNode, ExecutableNode> nodeBuilder) {
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext");
        this.nodeBuilder = Objects.requireNonNull(nodeBuilder, "nodeBuilder");
    }

    ExecutableNode buildNullCoalesce(BinaryOperationNode binaryOperation) {
        ExecutableNode left = buildNode(binaryOperation.left());
        if (ConstantNodeValues.isConstant(left)) {
            Object leftValue = ConstantNodeValues.value(left);
            if (leftValue != null) {
                return new ExecutableLiteral(leftValue);
            }
        }
        return new ExecutableNullCoalesce(left, buildNode(binaryOperation.right()));
    }

    ExecutableNode buildRegexOperation(BinaryOperationNode binaryOperation) {
        boolean negate = binaryOperation.operator() == BinaryOperator.REGEX_NOT_MATCH;
        ExecutableNode subjectNode = buildNode(binaryOperation.left());
        LiteralNode patternLiteral = (LiteralNode) binaryOperation.right();
        return new ExecutableRegexOp(
                subjectNode,
                Pattern.compile(LiteralMaterializer.unquoteStringLiteral(patternLiteral.value())),
                negate);
    }

    ExecutableNode buildBinaryOperation(BinaryOperationNode binaryOperation) {
        ExecutableNode left = buildNode(binaryOperation.left());
        ExecutableNode right = buildNode(binaryOperation.right());
        if (ConstantNodeValues.isConstant(left) && ConstantNodeValues.isConstant(right)) {
            Object result = OperatorEvaluator.evaluateBinary(
                    binaryOperation.operator(),
                    ConstantNodeValues.value(left),
                    ConstantNodeValues.value(right),
                    runtimeServices,
                    mathContext);
            return new ExecutableLiteral(result);
        }
        return new ExecutableBinaryOp(binaryOperation.operator(), left, right);
    }

    ExecutableNode buildTernaryOperation(TernaryOperationNode ternaryOperation) {
        ExecutableNode first = buildNode(ternaryOperation.first());
        ExecutableNode second = buildNode(ternaryOperation.second());
        ExecutableNode third = buildNode(ternaryOperation.third());
        if (ConstantNodeValues.isConstant(first)
                && ConstantNodeValues.isConstant(second)
                && ConstantNodeValues.isConstant(third)) {
            Object result = OperatorEvaluator.evaluateTernary(
                    ternaryOperation.operator(),
                    ConstantNodeValues.value(first),
                    ConstantNodeValues.value(second),
                    ConstantNodeValues.value(third),
                    runtimeServices);
            return new ExecutableLiteral(result);
        }
        return new ExecutableTernaryOp(ternaryOperation.operator(), first, second, third);
    }

    ExecutableNode buildUnaryOperation(UnaryOperationNode unaryOperation) {
        ExecutableNode operand = buildNode(unaryOperation.operand());
        if (ConstantNodeValues.isConstant(operand)) {
            Object result = OperatorEvaluator.evaluateUnary(
                    unaryOperation.operator(),
                    ConstantNodeValues.value(operand),
                    runtimeServices,
                    mathContext);
            return new ExecutableLiteral(result);
        }
        return new ExecutableUnaryOp(unaryOperation.operator(), operand);
    }

    ExecutableNode buildPostfixOperation(PostfixOperationNode postfixOperation) {
        ExecutableNode operand = buildNode(postfixOperation.operand());
        if (ConstantNodeValues.isConstant(operand)) {
            Object result = OperatorEvaluator.evaluatePostfix(
                    postfixOperation.operator(),
                    ConstantNodeValues.value(operand),
                    runtimeServices,
                    mathContext);
            return new ExecutableLiteral(result);
        }
        return new ExecutablePostfixOp(postfixOperation.operator(), operand);
    }

    private ExecutableNode buildNode(ExpressionNode node) {
        return nodeBuilder.apply(node);
    }
}
