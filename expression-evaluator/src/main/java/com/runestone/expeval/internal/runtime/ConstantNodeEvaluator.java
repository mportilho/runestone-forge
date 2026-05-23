package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.BinaryOperator;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

final class ConstantNodeEvaluator implements NodeEvaluator {

    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;

    ConstantNodeEvaluator(RuntimeServices runtimeServices, MathContext mathContext) {
        this.runtimeServices = runtimeServices;
        this.mathContext = mathContext;
    }

    @Override
    public Object evaluate(ExecutableNode node, ExecutionScope scope) {
        return switch (node) {
            case ExecutableLiteral literal -> literal.precomputed();
            case ExecutableDynamicLiteral ignored ->
                    throw new IllegalStateException("dynamic literals are not constant-foldable");
            case ExecutableIdentifier identifier -> evaluateIdentifier(identifier);
            case ExecutablePropertyChain chain -> PropertyChainOps.evaluatePropertyChain(
                    chain,
                    scope,
                    "<constant-folding>",
                    runtimeServices,
                    mathContext,
                    this);
            case ExecutableFunctionCall functionCall -> evaluateFunctionCall(functionCall, scope);
            case ExecutableBinaryOp binaryOp -> evaluateBinary(binaryOp, scope);
            case ExecutableTernaryOp ternaryOp -> OperatorEvaluator.evaluateTernary(
                    ternaryOp.operator(),
                    evaluate(ternaryOp.first(), scope),
                    evaluate(ternaryOp.second(), scope),
                    evaluate(ternaryOp.third(), scope),
                    runtimeServices);
            case ExecutableUnaryOp unaryOp -> OperatorEvaluator.evaluateUnary(
                    unaryOp.operator(), evaluate(unaryOp.operand(), scope), runtimeServices, mathContext);
            case ExecutablePostfixOp postfixOp -> OperatorEvaluator.evaluatePostfix(
                    postfixOp.operator(), evaluate(postfixOp.operand(), scope), runtimeServices, mathContext);
            case ExecutableConditional conditional -> evaluateConditional(conditional, scope);
            case ExecutableSimpleConditional conditional -> asBoolean(evaluate(conditional.condition(), scope))
                    ? evaluate(conditional.thenExpression(), scope)
                    : evaluate(conditional.elseExpression(), scope);
            case ExecutableVectorLiteral vectorLiteral -> evaluateVector(vectorLiteral, scope);
            case ExecutableNullCoalesce nullCoalesce -> {
                Object left = evaluate(nullCoalesce.left(), scope);
                yield left != null ? left : evaluate(nullCoalesce.right(), scope);
            }
            case ExecutableRegexOp regexOp -> {
                String subject = runtimeServices.asString(evaluate(regexOp.subject(), scope));
                boolean matches = regexOp.pattern().matcher(subject).find();
                yield regexOp.negate() != matches;
            }
        };
    }

    private Object evaluateIdentifier(ExecutableIdentifier identifier) {
        if (!LanguageSymbols.CURRENT_ELEMENT.equals(identifier.ref().name())) {
            throw new IllegalStateException("identifier is not constant-foldable: " + identifier.ref().name());
        }
        var context = FilterContextStack.INSTANCE.get().peek();
        if (context == null) {
            throw new IllegalStateException("@ used outside filter context during constant folding");
        }
        return context.isMapContext() ? context.mapValue() : context.element();
    }

    private Object evaluateFunctionCall(ExecutableFunctionCall functionCall, ExecutionScope scope) {
        if (functionCall.isFolded()) {
            return runtimeServices.coerceToResolvedType(functionCall.foldedResult(), functionCall.binding().returnType());
        }
        FunctionDescriptor descriptor = functionCall.binding().descriptor();
        Object[] arguments = new Object[descriptor.arity()];
        for (int index = 0; index < arguments.length; index++) {
            Object value = evaluate(functionCall.arguments().get(index), scope);
            arguments[index] = runtimeServices.coerce(value, descriptor.parameterTypes().get(index));
        }
        return runtimeServices.coerceToResolvedType(descriptor.invoke(arguments), functionCall.binding().returnType());
    }

    private Object evaluateBinary(ExecutableBinaryOp binaryOp, ExecutionScope scope) {
        Object left = evaluate(binaryOp.left(), scope);
        BinaryOperator operator = binaryOp.operator();
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
        Object right = evaluate(binaryOp.right(), scope);
        return OperatorEvaluator.evaluateBinary(operator, left, right, runtimeServices, mathContext);
    }

    private Object evaluateConditional(ExecutableConditional conditional, ExecutionScope scope) {
        for (int index = 0; index < conditional.conditions().size(); index++) {
            if (asBoolean(evaluate(conditional.conditions().get(index), scope))) {
                return evaluate(conditional.results().get(index), scope);
            }
        }
        return evaluate(conditional.elseExpression(), scope);
    }

    private List<Object> evaluateVector(ExecutableVectorLiteral vectorLiteral, ExecutionScope scope) {
        if (vectorLiteral.isFolded()) {
            return vectorLiteral.foldedValue();
        }
        List<Object> values = new ArrayList<>(vectorLiteral.elements().size());
        for (ExecutableNode element : vectorLiteral.elements()) {
            values.add(evaluate(element, scope));
        }
        return values;
    }

    private boolean asBoolean(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return runtimeServices.asBoolean(value);
    }
}
