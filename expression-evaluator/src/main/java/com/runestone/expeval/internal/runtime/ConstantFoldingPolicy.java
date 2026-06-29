package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.api.FunctionInvocationException;
import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.semantic.ResolvedFunctionBinding;
import com.runestone.expeval.internal.semantic.SemanticModel;
import com.runestone.expeval.internal.semantic.SymbolRef;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ConstantFoldingPolicy {

    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;
    private final Map<SymbolRef, Object> symbols = new HashMap<>();
    private final List<AuditEvent> variableReads = new ArrayList<>();
    private ConstantNodeEvaluator evaluator;

    ConstantFoldingPolicy(RuntimeServices runtimeServices, MathContext mathContext) {
        this.runtimeServices = runtimeServices;
        this.mathContext = mathContext;
    }

    void seedExternalConstants(SemanticModel model, ExternalSymbolCatalog externalSymbolCatalog) {
        model.externalSymbolsByName().forEach((name, symbolRef) -> {
            ExternalSymbolDescriptor descriptor = externalSymbolCatalog.findOrNull(name);
            if (descriptor != null && !descriptor.overridable()) {
                symbols.put(symbolRef,
                        runtimeServices.coerceToResolvedType(descriptor.defaultValue(), descriptor.declaredType()));
            }
        });
    }

    void publishAssignment(SymbolRef target, ExecutableNode value) {
        if (isConstantNode(value)) {
            symbols.put(target, constantValue(value));
            return;
        }
        symbols.remove(target);
    }

    ExecutableNode foldedSymbolReadOrNull(SymbolRef ref) {
        if (!symbols.containsKey(ref)) {
            return null;
        }
        Object value = symbols.get(ref);
        variableReads.add(new AuditEvent.VariableRead(ref.name(), false, value));
        return new ExecutableLiteral(value);
    }

    List<AuditEvent> foldedVariableReads() {
        return List.copyOf(variableReads);
    }

    boolean isConstantNode(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> true;
            case ExecutableFunctionCall functionCall -> functionCall.isFolded();
            case ExecutableVectorLiteral vectorLiteral -> vectorLiteral.isFolded();
            default -> false;
        };
    }

    Object constantValue(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral literal -> literal.precomputed();
            case ExecutableFunctionCall functionCall -> functionCall.foldedResult();
            case ExecutableVectorLiteral vectorLiteral -> vectorLiteral.foldedValue();
            default -> throw new IllegalStateException("not a constant node: " + node);
        };
    }

    ExecutableNode functionCall(ResolvedFunctionBinding binding, List<ExecutableNode> arguments) {
        FunctionDescriptor descriptor = binding.descriptor();
        if (!descriptor.isFoldable() || !allConstant(arguments)) {
            return ExecutableFunctionCall.of(binding, arguments);
        }

        int arity = descriptor.arity();
        Object[] args = new Object[arity];
        List<Class<?>> parameterTypes = descriptor.parameterTypes();
        for (int index = 0; index < arity; index++) {
            args[index] = runtimeServices.coerce(constantValue(arguments.get(index)), parameterTypes.get(index));
        }
        Object result = descriptor.invoke(args);
        return ExecutableFunctionCall.folded(binding, arguments, args, result);
    }

    ExecutableVectorLiteral vectorLiteral(List<ExecutableNode> elements) {
        if (!allConstant(elements)) {
            return new ExecutableVectorLiteral(elements);
        }
        List<Object> foldedValues = new ArrayList<>(elements.size());
        for (ExecutableNode element : elements) {
            foldedValues.add(constantValue(element));
        }
        return new ExecutableVectorLiteral(elements, foldedValues);
    }

    ExecutableNode foldPropertyChainPrefix(
            ExecutableNode root,
            List<ExecutablePropertyChain.ExecutableAccess> steps) {
        if (!isConstantNode(root) || steps.isEmpty()) {
            return new ExecutablePropertyChain(root, steps);
        }

        int foldedSteps = 0;
        Object foldedValue = constantValue(root);
        ConstantNodeEvaluator constantEvaluator = evaluator();
        for (int index = 0; index < steps.size(); index++) {
            ExecutablePropertyChain.ExecutableAccess access = steps.get(index);
            // Semantic barriers stay in the runtime suffix so folding never captures per-evaluation state.
            if (!isFoldableAccess(access)) {
                break;
            }
            ExecutablePropertyChain prefix = new ExecutablePropertyChain(root, steps.subList(0, index + 1));
            Object previousValue = foldedValue;
            try {
                foldedValue = PropertyChainOps.evaluatePropertyChain(
                        prefix,
                        null,
                        "<constant-folding>",
                        runtimeServices,
                        mathContext,
                        constantEvaluator);
            } catch (ExpressionEvaluationException | FunctionInvocationException | IllegalStateException exception) {
                // Preserve runtime failure timing: invalid constant navigation still fails during compute().
                break;
            }
            if (access instanceof ExecutablePropertyChain.ExecutableCollectionFunction collectionFunction) {
                recordFoldedCollectionFunctionCall(collectionFunction, previousValue, foldedValue, constantEvaluator);
            }
            foldedSteps = index + 1;
        }

        if (foldedSteps == 0) {
            return new ExecutablePropertyChain(root, steps);
        }
        if (foldedSteps == steps.size()) {
            return new ExecutableLiteral(foldedValue);
        }
        return new ExecutablePropertyChain(new ExecutableLiteral(foldedValue), steps.subList(foldedSteps, steps.size()));
    }

    private void recordFoldedCollectionFunctionCall(
            ExecutablePropertyChain.ExecutableCollectionFunction collectionFunction,
            Object current,
            Object result,
            ConstantNodeEvaluator constantEvaluator) {
        FunctionDescriptor descriptor = collectionFunction.binding().descriptor();
        List<Class<?>> parameterTypes = descriptor.parameterTypes();
        List<ExecutableNode> arguments = collectionFunction.arguments();
        Object[] auditArgs = new Object[arguments.size() + 1];
        auditArgs[0] = runtimeServices.coerce(current, parameterTypes.getFirst());
        for (int index = 0; index < arguments.size(); index++) {
            Object value = constantEvaluator.evaluate(arguments.get(index), null);
            auditArgs[index + 1] = runtimeServices.coerce(value, parameterTypes.get(index + 1));
        }
        variableReads.add(new AuditEvent.FunctionCall(descriptor.name(), auditArgs, result));
    }

    private ConstantNodeEvaluator evaluator() {
        ConstantNodeEvaluator current = evaluator;
        if (current == null) {
            current = new ConstantNodeEvaluator(runtimeServices, mathContext);
            evaluator = current;
        }
        return current;
    }

    private boolean isFoldableAccess(ExecutablePropertyChain.ExecutableAccess access) {
        return switch (access) {
            case ExecutablePropertyChain.ExecutableFieldGet ignored -> true;
            case ExecutablePropertyChain.ReflectivePropertyAccess ignored -> true;
            case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke ->
                    allFoldable(methodInvoke.arguments(), false);
            case ExecutablePropertyChain.ReflectiveMethodInvoke ignored -> false;
            case ExecutablePropertyChain.ExecutableIndexAccess indexAccess ->
                    isFoldableNode(indexAccess.index(), false);
            case ExecutablePropertyChain.ExecutableMapKeyAccess ignored -> true;
            case ExecutablePropertyChain.ExecutableSliceAccess sliceAccess ->
                    (sliceAccess.start() == null || isFoldableNode(sliceAccess.start(), false))
                    && (sliceAccess.end() == null || isFoldableNode(sliceAccess.end(), false));
            case ExecutablePropertyChain.ExecutableWildcard ignored -> true;
            case ExecutablePropertyChain.ExecutableFilterPredicate filterPredicate ->
                    isFoldableNode(filterPredicate.predicate(), true);
            case ExecutablePropertyChain.ExecutableDeepScan ignored -> false;
            case ExecutablePropertyChain.ExecutableCollectionFunction collectionFunction ->
                    collectionFunction.binding().descriptor() != null
                    && collectionFunction.binding().descriptor().isFoldable()
                    && allFoldable(collectionFunction.arguments(), false);
            case ExecutablePropertyChain.ExecutableMapProjection ignored -> true;
            case ExecutablePropertyChain.ExecutableVectorAggregation aggregation ->
                    aggregation.transform() == null || isFoldableNode(aggregation.transform(), true);
            case ExecutablePropertyChain.ExecutableVectorMap vectorMap ->
                    isFoldableNode(vectorMap.transform(), true);
        };
    }

    private boolean isFoldableNode(ExecutableNode node, boolean allowFilterContext) {
        return switch (node) {
            case ExecutableLiteral ignored -> true;
            case ExecutableDynamicLiteral ignored -> false;
            case ExecutableIdentifier identifier ->
                    allowFilterContext && LanguageSymbols.CURRENT_ELEMENT.equals(identifier.ref().name());
            case ExecutablePropertyChain chain -> isFoldablePropertyChainNode(chain, allowFilterContext);
            case ExecutableFunctionCall functionCall -> isFoldableFunctionCall(functionCall, allowFilterContext);
            case ExecutableBinaryOp binaryOp ->
                    isFoldableNode(binaryOp.left(), allowFilterContext)
                    && isFoldableNode(binaryOp.right(), allowFilterContext);
            case ExecutableTernaryOp ternaryOp ->
                    isFoldableNode(ternaryOp.first(), allowFilterContext)
                    && isFoldableNode(ternaryOp.second(), allowFilterContext)
                    && isFoldableNode(ternaryOp.third(), allowFilterContext);
            case ExecutableUnaryOp unaryOp -> isFoldableNode(unaryOp.operand(), allowFilterContext);
            case ExecutablePostfixOp postfixOp -> isFoldableNode(postfixOp.operand(), allowFilterContext);
            case ExecutableConditional conditional ->
                    allFoldable(conditional.conditions(), allowFilterContext)
                    && allFoldable(conditional.results(), allowFilterContext)
                    && isFoldableNode(conditional.elseExpression(), allowFilterContext);
            case ExecutableSimpleConditional conditional ->
                    isFoldableNode(conditional.condition(), allowFilterContext)
                    && isFoldableNode(conditional.thenExpression(), allowFilterContext)
                    && isFoldableNode(conditional.elseExpression(), allowFilterContext);
            case ExecutableVectorLiteral vectorLiteral ->
                    vectorLiteral.isFolded() || allFoldable(vectorLiteral.elements(), allowFilterContext);
            case ExecutableNullCoalesce nullCoalesce ->
                    isFoldableNode(nullCoalesce.left(), allowFilterContext)
                    && isFoldableNode(nullCoalesce.right(), allowFilterContext);
            case ExecutableRegexOp regexOp -> isFoldableNode(regexOp.subject(), allowFilterContext);
        };
    }

    private boolean isFoldablePropertyChainNode(ExecutablePropertyChain chain, boolean allowFilterContext) {
        if (!isFoldableNode(chain.root(), allowFilterContext)) {
            return false;
        }
        for (ExecutablePropertyChain.ExecutableAccess access : chain.chain()) {
            if (!isFoldableAccess(access)) {
                return false;
            }
        }
        return true;
    }

    private boolean isFoldableFunctionCall(ExecutableFunctionCall functionCall, boolean allowFilterContext) {
        if (functionCall.isFolded()) {
            return true;
        }
        return functionCall.binding().descriptor().isFoldable()
               && allFoldable(functionCall.arguments(), allowFilterContext);
    }

    private boolean allConstant(List<ExecutableNode> nodes) {
        for (ExecutableNode node : nodes) {
            if (!isConstantNode(node)) {
                return false;
            }
        }
        return true;
    }

    private boolean allFoldable(List<ExecutableNode> nodes, boolean allowFilterContext) {
        for (ExecutableNode node : nodes) {
            if (!isFoldableNode(node, allowFilterContext)) {
                return false;
            }
        }
        return true;
    }

    private static final class ConstantNodeEvaluator implements NodeEvaluator {

        private final RuntimeServices runtimeServices;
        private final MathContext mathContext;

        private ConstantNodeEvaluator(RuntimeServices runtimeServices, MathContext mathContext) {
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
            return RuntimeInvocationSupport.invokeFunction(
                    functionCall.binding(),
                    functionCall.arguments(),
                    scope,
                    runtimeServices,
                    this,
                    null);
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
}
