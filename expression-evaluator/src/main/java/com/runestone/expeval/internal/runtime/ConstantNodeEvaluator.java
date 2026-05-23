package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.LanguageSymbols;

import java.math.MathContext;

final class ConstantNodeEvaluator implements NodeEvaluator {

    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;
    private final StructuredExpressionEvaluator structuredExpressionEvaluator;

    ConstantNodeEvaluator(RuntimeServices runtimeServices, MathContext mathContext) {
        this.runtimeServices = runtimeServices;
        this.mathContext = mathContext;
        this.structuredExpressionEvaluator = new StructuredExpressionEvaluator(
                null, runtimeServices, mathContext, this);
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
            case ExecutableBinaryOp binaryOp -> structuredExpressionEvaluator.evaluateBinary(binaryOp, scope);
            case ExecutableTernaryOp ternaryOp -> structuredExpressionEvaluator.evaluateTernary(ternaryOp, scope);
            case ExecutableUnaryOp unaryOp -> structuredExpressionEvaluator.evaluateUnary(unaryOp, scope);
            case ExecutablePostfixOp postfixOp -> structuredExpressionEvaluator.evaluatePostfix(postfixOp, scope);
            case ExecutableConditional conditional -> structuredExpressionEvaluator.evaluateConditional(conditional, scope);
            case ExecutableSimpleConditional conditional ->
                    structuredExpressionEvaluator.evaluateSimpleConditional(conditional, scope);
            case ExecutableVectorLiteral vectorLiteral -> structuredExpressionEvaluator.evaluateVector(vectorLiteral, scope);
            case ExecutableNullCoalesce nullCoalesce ->
                    structuredExpressionEvaluator.evaluateNullCoalesce(nullCoalesce, scope);
            case ExecutableRegexOp regexOp -> structuredExpressionEvaluator.evaluateRegex(regexOp, scope);
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

}
