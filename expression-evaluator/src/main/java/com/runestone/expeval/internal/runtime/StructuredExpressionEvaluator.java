package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.*;

import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.internal.ast.BinaryOperator;
import org.jspecify.annotations.Nullable;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

final class StructuredExpressionEvaluator {

    private final @Nullable String source;
    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;
    private final NodeEvaluator nodeEvaluator;

    StructuredExpressionEvaluator(
            @Nullable String source,
            RuntimeServices runtimeServices,
            MathContext mathContext,
            NodeEvaluator nodeEvaluator) {
        this.source = source;
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext");
        this.nodeEvaluator = Objects.requireNonNull(nodeEvaluator, "nodeEvaluator");
    }

    Object evaluateConditional(ExecutableConditional node, ExecutionScope scope) {
        List<ExecutableNode> conditions = node.conditions();
        for (int index = 0; index < conditions.size(); index++) {
            if (asBoolean(nodeEvaluator.evaluate(conditions.get(index), scope))) {
                return nodeEvaluator.evaluate(node.results().get(index), scope);
            }
        }
        return nodeEvaluator.evaluate(node.elseExpression(), scope);
    }

    Object evaluateSimpleConditional(ExecutableSimpleConditional node, ExecutionScope scope) {
        if (asBoolean(nodeEvaluator.evaluate(node.condition(), scope))) {
            return nodeEvaluator.evaluate(node.thenExpression(), scope);
        }
        return nodeEvaluator.evaluate(node.elseExpression(), scope);
    }

    Object evaluateUnary(ExecutableUnaryOp node, ExecutionScope scope) {
        Object operand = nodeEvaluator.evaluate(node.operand(), scope);
        return OperatorEvaluator.evaluateUnary(node.operator(), operand, runtimeServices, mathContext);
    }

    Object evaluateBinary(ExecutableBinaryOp node, ExecutionScope scope) {
        Object left = nodeEvaluator.evaluate(node.left(), scope);
        BinaryOperator operator = node.operator();
        if (operator == BinaryOperator.AND || operator == BinaryOperator.NAND) {
            boolean leftBool = asBoolean(left);
            if (!leftBool) {
                return operator == BinaryOperator.NAND;
            }
        } else if (operator == BinaryOperator.OR || operator == BinaryOperator.NOR) {
            boolean leftBool = asBoolean(left);
            if (leftBool) {
                return operator == BinaryOperator.OR;
            }
        }
        Object right = nodeEvaluator.evaluate(node.right(), scope);
        return OperatorEvaluator.evaluateBinary(operator, left, right, runtimeServices, mathContext);
    }

    Object evaluateTernary(ExecutableTernaryOp node, ExecutionScope scope) {
        Object value = nodeEvaluator.evaluate(node.first(), scope);
        Object lower = nodeEvaluator.evaluate(node.second(), scope);
        Object upper = nodeEvaluator.evaluate(node.third(), scope);
        return OperatorEvaluator.evaluateTernary(node.operator(), value, lower, upper, runtimeServices);
    }

    Object evaluatePostfix(ExecutablePostfixOp node, ExecutionScope scope) {
        Object operand = nodeEvaluator.evaluate(node.operand(), scope);
        return OperatorEvaluator.evaluatePostfix(node.operator(), operand, runtimeServices, mathContext);
    }

    Object evaluateNullCoalesce(ExecutableNullCoalesce node, ExecutionScope scope) {
        Object left = nodeEvaluator.evaluate(node.left(), scope);
        return left != null ? left : nodeEvaluator.evaluate(node.right(), scope);
    }

    Object evaluateRegex(ExecutableRegexOp node, ExecutionScope scope) {
        String subject = asString(nodeEvaluator.evaluate(node.subject(), scope));
        boolean matches = node.pattern().matcher(subject).find();
        return node.negate() != matches;
    }

    List<Object> evaluateVector(ExecutableVectorLiteral node, ExecutionScope scope) {
        if (node.isFolded()) {
            return node.foldedValue();
        }
        List<Object> elements = new ArrayList<>(node.elements().size());
        for (ExecutableNode element : node.elements()) {
            elements.add(nodeEvaluator.evaluate(element, scope));
        }
        return elements;
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        try {
            return runtimeServices.asBoolean(value);
        } catch (IllegalStateException exception) {
            if (source == null) {
                throw exception;
            }
            throw new ExpressionEvaluationException(source, "NULL_VALUE", "cannot use null value as a boolean", null);
        }
    }

    private String asString(Object value) {
        if (value instanceof String stringValue) {
            return stringValue;
        }
        try {
            return runtimeServices.asString(value);
        } catch (IllegalStateException exception) {
            if (source == null) {
                throw exception;
            }
            throw new ExpressionEvaluationException(source, "NULL_VALUE", "cannot use null value as a string", null);
        }
    }
}
