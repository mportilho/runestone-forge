package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.catalog.*;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.*;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.navigation.NavigationMode;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;
import com.runestone.expeval.types.*;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class ExecutionPlanBuilder {

    ExecutionPlan build(SemanticModel model, RuntimeServices runtimeServices, ExternalSymbolCatalog externalSymbolCatalog, TypeHintCatalog typeHintCatalog, MathContext mathContext) {
        // 1. Assign indices to symbols
        SymbolIndexAllocator.assignIndices(model);

        ExpressionFileNode ast = model.ast();
        // Process assignments sequentially so each one can propagate its constant value
        // to later assignments and to the result expression.
        FoldContext foldContext = new FoldContext();

        foldContext.symbols.putAll(ExternalBindingPlanner.seedNonOverridableConstants(
                model,
                externalSymbolCatalog,
                runtimeServices));

        List<ExecutableAssignment> assignments = new ArrayList<>();
        for (AssignmentNode assignment : ast.assignments()) {
            assignments.add(buildAssignment(assignment, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext));
        }
        ExecutableNode resultNode = ast.resultExpression() != null
                ? buildNode(ast.resultExpression(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext)
                : null;
        List<AuditEvent> foldedVariableReads = List.copyOf(foldContext.variableReads);
        int maxAuditEvents = AuditEventEstimator.estimate(assignments, resultNode, foldedVariableReads.size());

        int externalSymbolsCount = model.externalSymbolsByName().size();
        Object[] defaults = ExternalBindingPlanner.seedDefaults(model, externalSymbolCatalog, runtimeServices);
        var externalBindingPlans = ExternalBindingPlanner.seedBindingPlans(model, externalSymbolCatalog);

        return new ExecutionPlan(assignments, resultNode, defaults, externalBindingPlans, externalSymbolsCount, maxAuditEvents, foldedVariableReads);
    }

    private ExecutableAssignment buildAssignment(
            AssignmentNode assignment,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext,
            FoldContext foldContext) {
        return switch (assignment) {
            case SimpleAssignmentNode s -> {
                SymbolRef target = model.internalSymbolsByName().get(s.targetName());
                ExecutableNode value = buildNode(s.value(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (ConstantNodeValues.isConstant(value)) {
                    foldContext.symbols.put(target, ConstantNodeValues.value(value));
                } else {
                    foldContext.symbols.remove(target);
                }
                yield new ExecutableSimpleAssignment(target, value);
            }
            case DestructuringAssignmentNode d -> {
                List<SymbolRef> targets = d.targetNames().stream()
                        .map(name -> model.internalSymbolsByName().get(name))
                        .toList();
                yield new ExecutableDestructuringAssignment(
                        targets,
                        buildNode(d.value(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext)
                );
            }
        };
    }

    private ExecutableNode buildNode(
            ExpressionNode node,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext,
            FoldContext foldContext) {
        return switch (node) {
            case LiteralNode lit -> LiteralMaterializer.build(lit, model);
            case IdentifierNode id -> buildIdentifier(id, model, foldContext);
            case PropertyChainNode chain -> buildPropertyChain(
                    chain, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
            case FunctionCallNode f ->
                    buildFunctionCall(f, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
            case BinaryOperationNode b when b.operator() == BinaryOperator.NULL_COALESCE -> {
                ExecutableNode left = buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (ConstantNodeValues.isConstant(left)) {
                    Object leftVal = ConstantNodeValues.value(left);
                    if (leftVal != null) {
                        yield new ExecutableLiteral(leftVal);
                    }
                }
                yield new ExecutableNullCoalesce(
                        left,
                        buildNode(b.right(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext)
                );
            }
            case BinaryOperationNode b when b.operator() == BinaryOperator.REGEX_MATCH
                                         || b.operator() == BinaryOperator.REGEX_NOT_MATCH -> {
                boolean negate = b.operator() == BinaryOperator.REGEX_NOT_MATCH;
                ExecutableNode subjectNode = buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                LiteralNode patternLit = (LiteralNode) b.right();
                yield new ExecutableRegexOp(subjectNode, Pattern.compile(LiteralMaterializer.unquoteStringLiteral(patternLit.value())), negate);
            }
            case BinaryOperationNode b -> {
                ExecutableNode left = buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                ExecutableNode right = buildNode(b.right(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (ConstantNodeValues.isConstant(left) && ConstantNodeValues.isConstant(right)) {
                    Object leftVal = ConstantNodeValues.value(left);
                    Object rightVal = ConstantNodeValues.value(right);
                    Object result = OperatorEvaluator.evaluateBinary(b.operator(), leftVal, rightVal, runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableBinaryOp(b.operator(), left, right);
            }
            case TernaryOperationNode t -> {
                ExecutableNode first = buildNode(t.first(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                ExecutableNode second = buildNode(t.second(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                ExecutableNode third = buildNode(t.third(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (ConstantNodeValues.isConstant(first) && ConstantNodeValues.isConstant(second) && ConstantNodeValues.isConstant(third)) {
                    Object result = OperatorEvaluator.evaluateTernary(t.operator(), ConstantNodeValues.value(first), ConstantNodeValues.value(second), ConstantNodeValues.value(third), runtimeServices);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableTernaryOp(t.operator(), first, second, third);
            }
            case UnaryOperationNode u -> {
                ExecutableNode operand = buildNode(u.operand(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (ConstantNodeValues.isConstant(operand)) {
                    Object result = OperatorEvaluator.evaluateUnary(u.operator(), ConstantNodeValues.value(operand), runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableUnaryOp(u.operator(), operand);
            }
            case PostfixOperationNode p -> {
                ExecutableNode operand = buildNode(p.operand(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (ConstantNodeValues.isConstant(operand)) {
                    Object result = OperatorEvaluator.evaluatePostfix(p.operator(), ConstantNodeValues.value(operand), runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutablePostfixOp(p.operator(), operand);
            }
            case ConditionalNode c -> {
                List<ExecutableNode> conditions = new ArrayList<>();
                List<ExecutableNode> results = new ArrayList<>();
                ExecutableNode elseExpr = null;

                for (int i = 0; i < c.conditions().size(); i++) {
                    ExecutableNode condNode = buildNode(c.conditions().get(i), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                    if (ConstantNodeValues.isConstant(condNode)) {
                        Object val = ConstantNodeValues.value(condNode);
                        if (Boolean.TRUE.equals(val)) {
                            elseExpr = buildNode(c.results().get(i), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                            break;
                        }
                        // If constant false, just skip this branch
                    } else {
                        conditions.add(condNode);
                        results.add(buildNode(c.results().get(i), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext));
                    }
                }

                if (elseExpr == null) {
                    elseExpr = buildNode(c.elseExpression(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                }

                if (conditions.isEmpty()) {
                    yield elseExpr;
                }

                if (conditions.size() == 1) {
                    yield new ExecutableSimpleConditional(conditions.get(0), results.get(0), elseExpr);
                }

                yield new ExecutableConditional(conditions, results, elseExpr);
            }
            case VectorLiteralNode v -> {
                List<ExecutableNode> elements = v.elements().stream()
                        .map(e -> buildNode(e, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext))
                        .toList();
                if (elements.stream().allMatch(ConstantNodeValues::isConstant)) {
                    List<Object> foldedValues = elements.stream().map(ConstantNodeValues::value).toList();
                    yield new ExecutableVectorLiteral(elements, foldedValues);
                }
                yield new ExecutableVectorLiteral(elements);
            }
        };
    }

    private ExecutableNode buildPropertyChain(
            PropertyChainNode node,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext,
            FoldContext foldContext) {

        // '@' root has no external symbol binding; create a special sentinel identifier.
        ExecutableNode root;
        if (LanguageSymbols.CURRENT_ELEMENT.equals(node.rootIdentifier())) {
            root = new ExecutableIdentifier(new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL), node.sourceSpan());
        } else {
            SymbolRef rootRef = model.findSymbol(node.nodeId())
                    .orElseThrow(() -> new IllegalStateException(
                            "missing symbol for property chain '" + node.rootIdentifier() + "'"));
            if (foldContext.symbols.containsKey(rootRef)) {
                Object value = foldContext.symbols.get(rootRef);
                foldContext.variableReads.add(new AuditEvent.VariableRead(rootRef.name(), false, value));
                root = new ExecutableLiteral(value);
            } else {
                root = new ExecutableIdentifier(rootRef, node.sourceSpan());
            }
        }

        ResolvedType currentType = LanguageSymbols.CURRENT_ELEMENT.equals(node.rootIdentifier())
                ? UnknownType.INSTANCE
                : PropertyChainRootTypeResolver.resolve(
                        model.findSymbol(node.nodeId()).orElse(new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL)),
                        model, externalSymbolCatalog);

        if (NavigationStepClassifier.isLegacyAccessChain(node.chain())) {
            return buildLegacyPropertyChain(
                    node,
                    model,
                    runtimeServices,
                    externalSymbolCatalog,
                    typeHintCatalog,
                    mathContext,
                    foldContext,
                    root,
                    currentType
            );
        }

        List<ExecutablePropertyChain.ExecutableAccess> steps = new ArrayList<>(node.chain().size());

        for (PropertyChainNode.MemberAccess access : node.chain()) {
            boolean safe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;

            // ----- New navigation step types (take priority over typed/reflective paths) -----
            switch (access) {
                case PropertyChainNode.CollectionIndexStep cis -> {
                    ExecutableNode idx = buildNode(cis.index(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                    steps.add(new ExecutablePropertyChain.ExecutableIndexAccess(idx));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.MapKeyStep mks -> {
                    steps.add(new ExecutablePropertyChain.ExecutableMapKeyAccess(mks.key()));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.CollectionSliceStep css -> {
                    ExecutableNode start = css.start() != null
                            ? buildNode(css.start(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext)
                            : null;
                    ExecutableNode end = css.end() != null
                            ? buildNode(css.end(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext)
                            : null;
                    steps.add(new ExecutablePropertyChain.ExecutableSliceAccess(start, end));
                    continue;
                }
                case PropertyChainNode.WildcardStep ignored -> {
                    steps.add(new ExecutablePropertyChain.ExecutableWildcard());
                    continue;
                }
                case PropertyChainNode.FilterPredicateStep fps -> {
                    ExecutableNode predicate = buildNode(fps.predicate(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                    steps.add(new ExecutablePropertyChain.ExecutableFilterPredicate(predicate));
                    continue;
                }
                case PropertyChainNode.DeepScanStep dss -> {
                    steps.add(new ExecutablePropertyChain.ExecutableDeepScan(dss.propertyName()));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.CollectionFunctionStep cfs -> {
                    List<ExecutableNode> args = cfs.arguments().stream()
                            .map(a -> buildNode(a, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext))
                            .toList();
                    ResolvedFunctionBinding binding = model.findFunctionBinding(node.nodeId()).orElse(null);
                    NavigationMode resultMode = NavigationMode.SCALAR;
                    ResolvedType returnType = UnknownType.INSTANCE;
                    if (binding != null) {
                        returnType = binding.returnType();
                        if (returnType instanceof com.runestone.expeval.types.CollectionType
                                || returnType == VectorType.INSTANCE) {
                            resultMode = NavigationMode.COLLECTION;
                        } else if (returnType instanceof com.runestone.expeval.types.MapType) {
                            resultMode = NavigationMode.MAP;
                        }
                        steps.add(new ExecutablePropertyChain.ExecutableCollectionFunction(binding, args, resultMode));
                    } else {
                        // Fallback: build reflective call — unknown return mode
                        steps.add(new ExecutablePropertyChain.ExecutableCollectionFunction(
                                new ResolvedFunctionBinding(null, null, UnknownType.INSTANCE), args, resultMode));
                    }
                    currentType = returnType;
                    continue;
                }
                case PropertyChainNode.MapProjectionStep mps -> {
                    steps.add(new ExecutablePropertyChain.ExecutableMapProjection(mps.kind()));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.VectorMapStep vms -> {
                    ExecutableNode execTransform = buildNode(vms.transform(), model, runtimeServices,
                            externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                    steps.add(new ExecutablePropertyChain.ExecutableVectorMap(execTransform));
                    currentType = VectorType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.VectorAggregationStep vas -> {
                    // O5: fold ..values()..count() / ..keys()..count() — the runtime already handles
                    // Map+COUNT via map.size(), so materialising the list first is unnecessary.
                    if (vas.kind() == VectorAggregationKind.COUNT
                            && !steps.isEmpty()
                            && steps.getLast() instanceof ExecutablePropertyChain.ExecutableMapProjection) {
                        steps.removeLast();
                    }
                    ExecutableNode execTransform = vas.transform() == null ? null
                            : buildNode(vas.transform(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                    steps.add(new ExecutablePropertyChain.ExecutableVectorAggregation(vas.kind(), execTransform));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                default -> { /* fall through to typed/reflective path below */ }
            }

            // ----- Existing typed / reflective access -----
            List<ExecutableNode> argumentNodes = switch (access) {
                case PropertyChainNode.MethodCallAccess methodCall ->
                        methodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext))
                                .toList();
                case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                        safeMethodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext))
                                .toList();
                default -> List.of();
            };

            if (currentType instanceof ObjectType objectType) {
                TypeMetadata metadata = typeHintCatalog.find(objectType.javaClass()).orElse(null);
                if (metadata != null) {
                    TypedAccessPlanner.TypedAccessStep typedStep = TypedAccessPlanner.build(
                            access,
                            argumentNodes,
                            model,
                            metadata,
                            typeHintCatalog,
                            safe
                    );
                    steps.add(typedStep.access());
                    currentType = typedStep.nextType();
                    continue;
                }
            }

            steps.add(ReflectiveAccessPlanner.build(access, argumentNodes, safe));
            currentType = UnknownType.INSTANCE;
        }
        return PropertyChainPrefixFolder.fold(root, steps, runtimeServices, mathContext);
    }

    private ExecutableNode buildLegacyPropertyChain(
            PropertyChainNode node,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext,
            FoldContext foldContext,
            ExecutableNode root,
            ResolvedType currentType) {
        List<ExecutablePropertyChain.ExecutableAccess> steps = new ArrayList<>(node.chain().size());

        for (PropertyChainNode.MemberAccess access : node.chain()) {
            boolean safe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;
            List<ExecutableNode> argumentNodes = switch (access) {
                case PropertyChainNode.MethodCallAccess methodCall ->
                        methodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext))
                                .toList();
                case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                        safeMethodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext))
                                .toList();
                default -> List.of();
            };

            if (currentType instanceof ObjectType objectType) {
                TypeMetadata metadata = typeHintCatalog.find(objectType.javaClass()).orElse(null);
                if (metadata != null) {
                    TypedAccessPlanner.TypedAccessStep typedStep = TypedAccessPlanner.build(
                            access,
                            argumentNodes,
                            model,
                            metadata,
                            typeHintCatalog,
                            safe
                    );
                    steps.add(typedStep.access());
                    currentType = typedStep.nextType();
                    continue;
                }
            }

            steps.add(ReflectiveAccessPlanner.build(access, argumentNodes, safe));
            currentType = UnknownType.INSTANCE;
        }
        return PropertyChainPrefixFolder.fold(root, steps, runtimeServices, mathContext);
    }

    private ExecutableNode buildIdentifier(IdentifierNode id, SemanticModel model, FoldContext foldContext) {
        // '@' is the filter-predicate current-element sentinel — no model symbol, always dynamic
        if (LanguageSymbols.CURRENT_ELEMENT.equals(id.name())) {
            return new ExecutableIdentifier(new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL), id.sourceSpan());
        }
        SymbolRef ref = model.findSymbol(id.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing symbol for identifier '" + id.name() + "'"));
        if (foldContext.symbols.containsKey(ref)) {
            Object value = foldContext.symbols.get(ref);
            foldContext.variableReads.add(new AuditEvent.VariableRead(ref.name(), false, value));
            return new ExecutableLiteral(value);
        }
        return new ExecutableIdentifier(ref, id.sourceSpan());
    }

    private ExecutableNode buildFunctionCall(
            FunctionCallNode f,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext,
            FoldContext foldContext) {
        ResolvedFunctionBinding binding = model.findFunctionBinding(f.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing function binding for '" + f.functionName() + "'"));
        List<ExecutableNode> arguments = f.arguments().stream()
                .map(arg -> buildNode(arg, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext))
                .toList();

        FunctionDescriptor descriptor = binding.descriptor();
        if (descriptor.isFoldable() && arguments.stream().allMatch(ConstantNodeValues::isConstant)) {
            int arity = descriptor.arity();
            Object[] args = new Object[arity];
            for (int i = 0; i < arity; i++) {
                args[i] = runtimeServices.coerce(
                        ConstantNodeValues.value(arguments.get(i)),
                        descriptor.parameterTypes().get(i));
            }
            Object result = descriptor.invoke(args);
            return ExecutableFunctionCall.folded(binding, arguments, args, result);
        }

        return ExecutableFunctionCall.of(binding, arguments);
    }

    private static final class FoldContext {
        final Map<SymbolRef, Object> symbols = new HashMap<>();
        final List<AuditEvent> variableReads = new ArrayList<>();
    }
}
