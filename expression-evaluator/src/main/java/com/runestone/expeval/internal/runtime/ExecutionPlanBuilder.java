package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.api.ExpressionEvaluationException;
import com.runestone.expeval.api.FunctionInvocationException;
import com.runestone.expeval.catalog.*;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.*;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.navigation.NavigationMode;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;
import com.runestone.expeval.internal.semantic.ResolvedFunctionBinding;
import com.runestone.expeval.internal.semantic.SemanticModel;
import com.runestone.expeval.internal.semantic.SymbolKind;
import com.runestone.expeval.internal.semantic.SymbolRef;
import com.runestone.expeval.types.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class ExecutionPlanBuilder {

    ExecutionPlan build(SemanticModel model, RuntimeServices runtimeServices, ExternalSymbolCatalog externalSymbolCatalog, TypeHintCatalog typeHintCatalog, MathContext mathContext) {
        // 1. Assign indices to symbols
        assignIndices(model);

        ExpressionFileNode ast = model.ast();
        // Process assignments sequentially so each one can propagate its constant value
        // to later assignments and to the result expression.
        FoldContext foldContext = new FoldContext();

        model.externalSymbolsByName().forEach((name, symbolRef) -> {
            ExternalSymbolDescriptor descriptor = externalSymbolCatalog.findOrNull(name);
            if (descriptor != null && !descriptor.overridable()) {
                foldContext.symbols.put(symbolRef, runtimeServices.coerceToResolvedType(descriptor.defaultValue(), descriptor.declaredType()));
            }
        });

        List<ExecutableAssignment> assignments = new ArrayList<>();
        for (AssignmentNode assignment : ast.assignments()) {
            assignments.add(buildAssignment(assignment, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext));
        }
        ExecutableNode resultNode = ast.resultExpression() != null
                ? buildNode(ast.resultExpression(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext)
                : null;
        List<AuditEvent> foldedVariableReads = List.copyOf(foldContext.variableReads);
        int maxAuditEvents = countMaxAuditEvents(assignments, resultNode) + foldedVariableReads.size();

        int externalSymbolsCount = model.externalSymbolsByName().size();
        Object[] defaults = seedDefaults(model, externalSymbolCatalog, runtimeServices, externalSymbolsCount);
        Map<String, ExternalBindingPlan> externalBindingPlans = seedExternalBindingPlans(model, externalSymbolCatalog);

        return new ExecutionPlan(assignments, resultNode, defaults, externalBindingPlans, externalSymbolsCount, maxAuditEvents, foldedVariableReads);
    }

    /**
     * Assigns stable, zero-based integer indices to all internal and external symbols in
     * {@code model}. Symbols within each group are processed in alphabetical order to ensure
     * that indices are deterministic across JVM runs and independent of Map iteration order.
     *
     * <p>This method must be called <strong>exactly once</strong> per {@link SemanticModel},
     * before any array-backed structures (defaults, binding plans) are built from that model.
     * The resulting indices are embedded into the {@link SymbolRef} objects and must agree with
     * the array sizes and positions produced by {@code seedDefaults()} and
     * {@code seedExternalBindingPlans()}. Do NOT change the ordering without updating those methods.
     */
    private void assignIndices(SemanticModel model) {
        int internalIdx = 0;
        List<String> sortedInternalNames = new ArrayList<>(model.internalSymbolsByName().keySet());
        // Alphabetical order guarantees a stable name→position mapping across separate compilations
        // of the same expression, regardless of the underlying Map's iteration order.
        Collections.sort(sortedInternalNames);
        for (String name : sortedInternalNames) {
            model.internalSymbolsByName().get(name).setIndex(internalIdx++);
        }

        int externalIdx = 0;
        List<String> sortedExternalNames = new ArrayList<>(model.externalSymbolsByName().keySet());
        // Same rationale: stable ordering ensures seedDefaults() and seedExternalBindingPlans()
        // write to the correct array positions for every symbol.
        Collections.sort(sortedExternalNames);
        for (String name : sortedExternalNames) {
            model.externalSymbolsByName().get(name).setIndex(externalIdx++);
        }
    }

    private static Object[] seedDefaults(SemanticModel semanticModel, ExternalSymbolCatalog catalog, RuntimeServices runtimeServices, int externalSymbolsCount) {
        if (externalSymbolsCount == 0) {
            return new Object[0];
        }
        Object[] defaults = new Object[externalSymbolsCount];
        java.util.Arrays.fill(defaults, ExecutionScope.UNBOUND);
        semanticModel.externalSymbolsByName().forEach((name, symbolRef) -> {
            catalog.find(name)
                    .ifPresent(descriptor -> defaults[symbolRef.index()] =
                            runtimeServices.coerceToResolvedType(descriptor.defaultValue(), descriptor.declaredType())
                    );
        });
        return defaults;
    }

    private static Map<String, ExternalBindingPlan> seedExternalBindingPlans(SemanticModel semanticModel, ExternalSymbolCatalog catalog) {
        if (semanticModel.externalSymbolsByName().isEmpty()) {
            return Map.of();
        }
        Map<String, ExternalBindingPlan> bindings = new HashMap<>();
        semanticModel.externalSymbolsByName().forEach((name, symbolRef) -> {
            ExternalSymbolDescriptor descriptor = catalog.findOrNull(name);
            bindings.put(name, new ExternalBindingPlan(
                    symbolRef,
                    descriptor != null ? descriptor.declaredType() : null,
                    descriptor == null || descriptor.overridable()
            ));
        });
        return bindings;
    }

    private int countMaxAuditEvents(List<ExecutableAssignment> assignments, ExecutableNode resultExpression) {
        int count = 0;
        for (ExecutableAssignment assignment : assignments) {
            count += switch (assignment) {
                case ExecutableSimpleAssignment s -> 1 + countNodeEvents(s.value());
                case ExecutableDestructuringAssignment d -> d.targets().size() + countNodeEvents(d.value());
            };
        }
        if (resultExpression != null) {
            count += countNodeEvents(resultExpression);
        }
        return count;
    }

    private int countNodeEvents(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> 0;
            case ExecutableDynamicLiteral ignored -> 1;
            case ExecutableIdentifier ignored -> 1;
            case ExecutablePropertyChain chain -> countPropertyChainEvents(chain);
            case ExecutableFunctionCall f -> {
                int sum = 1;
                for (ExecutableNode arg : f.arguments()) sum += countNodeEvents(arg);
                yield sum;
            }
            case ExecutableBinaryOp b -> countNodeEvents(b.left()) + countNodeEvents(b.right());
            case ExecutableTernaryOp t -> countNodeEvents(t.first()) + countNodeEvents(t.second()) + countNodeEvents(t.third());
            case ExecutableUnaryOp u -> countNodeEvents(u.operand());
            case ExecutablePostfixOp p -> countNodeEvents(p.operand());
            case ExecutableConditional c -> {
                int condCost = 0;
                for (ExecutableNode cond : c.conditions()) condCost += countNodeEvents(cond);

                int maxBranchCost = countNodeEvents(c.elseExpression());
                for (ExecutableNode res : c.results()) {
                    maxBranchCost = Math.max(maxBranchCost, countNodeEvents(res));
                }
                yield condCost + maxBranchCost;
            }
            case ExecutableSimpleConditional sc -> {
                int condCost = countNodeEvents(sc.condition());
                int maxBranchCost = Math.max(countNodeEvents(sc.thenExpression()), countNodeEvents(sc.elseExpression()));
                yield condCost + maxBranchCost;
            }
            case ExecutableVectorLiteral v -> {
                int sum = 0;
                for (ExecutableNode el : v.elements()) sum += countNodeEvents(el);
                yield sum;
            }
            case ExecutableNullCoalesce nc ->
                    countNodeEvents(nc.left()) + countNodeEvents(nc.right());
            case ExecutableRegexOp r -> countNodeEvents(r.subject());
        };
    }

    private int countPropertyChainEvents(ExecutablePropertyChain chain) {
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
                continue;
            }
            if (access instanceof ExecutablePropertyChain.ExecutableCollectionFunction collectionFunction) {
                count++;
                for (ExecutableNode argument : collectionFunction.arguments()) {
                    count += countNodeEvents(argument);
                }
            }
        }
        return count;
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
                if (isConstantNode(value)) {
                    foldContext.symbols.put(target, constantValue(value));
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
            case LiteralNode lit -> buildLiteral(lit, model);
            case IdentifierNode id -> buildIdentifier(id, model, foldContext);
            case PropertyChainNode chain -> buildPropertyChain(
                    chain, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
            case FunctionCallNode f ->
                    buildFunctionCall(f, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
            case BinaryOperationNode b when b.operator() == BinaryOperator.NULL_COALESCE -> {
                ExecutableNode left = buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (isConstantNode(left)) {
                    Object leftVal = constantValue(left);
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
                yield new ExecutableRegexOp(subjectNode, Pattern.compile(unquote(patternLit.value())), negate);
            }
            case BinaryOperationNode b -> {
                ExecutableNode left = buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                ExecutableNode right = buildNode(b.right(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (isConstantNode(left) && isConstantNode(right)) {
                    Object leftVal = constantValue(left);
                    Object rightVal = constantValue(right);
                    Object result = OperatorEvaluator.evaluateBinary(b.operator(), leftVal, rightVal, runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableBinaryOp(b.operator(), left, right);
            }
            case TernaryOperationNode t -> {
                ExecutableNode first = buildNode(t.first(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                ExecutableNode second = buildNode(t.second(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                ExecutableNode third = buildNode(t.third(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (isConstantNode(first) && isConstantNode(second) && isConstantNode(third)) {
                    Object result = OperatorEvaluator.evaluateTernary(t.operator(), constantValue(first), constantValue(second), constantValue(third), runtimeServices);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableTernaryOp(t.operator(), first, second, third);
            }
            case UnaryOperationNode u -> {
                ExecutableNode operand = buildNode(u.operand(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (isConstantNode(operand)) {
                    Object result = OperatorEvaluator.evaluateUnary(u.operator(), constantValue(operand), runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableUnaryOp(u.operator(), operand);
            }
            case PostfixOperationNode p -> {
                ExecutableNode operand = buildNode(p.operand(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldContext);
                if (isConstantNode(operand)) {
                    Object result = OperatorEvaluator.evaluatePostfix(p.operator(), constantValue(operand), runtimeServices, mathContext);
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
                    if (isConstantNode(condNode)) {
                        Object val = constantValue(condNode);
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
                if (elements.stream().allMatch(this::isConstantNode)) {
                    List<Object> foldedValues = elements.stream().map(this::constantValue).toList();
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
                : resolveRootType(
                        model.findSymbol(node.nodeId()).orElse(new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL)),
                        model, externalSymbolCatalog);

        if (isLegacyAccessChain(node.chain())) {
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
                    TypedAccessStep typedStep = buildTypedAccessStep(
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

            steps.add(buildReflectiveAccess(access, argumentNodes, safe));
            currentType = UnknownType.INSTANCE;
        }
        return foldPropertyChainPrefix(root, steps, runtimeServices, mathContext, foldContext);
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
                    TypedAccessStep typedStep = buildTypedAccessStep(
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

            steps.add(buildReflectiveAccess(access, argumentNodes, safe));
            currentType = UnknownType.INSTANCE;
        }
        return foldPropertyChainPrefix(root, steps, runtimeServices, mathContext, foldContext);
    }

    private ExecutableNode foldPropertyChainPrefix(
            ExecutableNode root,
            List<ExecutablePropertyChain.ExecutableAccess> steps,
            RuntimeServices runtimeServices,
            MathContext mathContext,
            FoldContext foldContext) {
        if (!isConstantNode(root) || steps.isEmpty()) {
            return new ExecutablePropertyChain(root, steps);
        }

        int foldedSteps = 0;
        Object foldedValue = constantValue(root);
        ConstantNodeEvaluator evaluator = new ConstantNodeEvaluator(runtimeServices, mathContext);
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
                        evaluator);
            } catch (ExpressionEvaluationException | FunctionInvocationException | IllegalStateException exception) {
                // Preserve runtime failure timing: invalid constant navigation still fails during compute().
                break;
            }
            if (access instanceof ExecutablePropertyChain.ExecutableCollectionFunction collectionFunction) {
                recordFoldedCollectionFunctionCall(
                        foldContext,
                        collectionFunction,
                        previousValue,
                        foldedValue,
                        runtimeServices,
                        evaluator);
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
            FoldContext foldContext,
            ExecutablePropertyChain.ExecutableCollectionFunction collectionFunction,
            Object current,
            Object result,
            RuntimeServices runtimeServices,
            ConstantNodeEvaluator evaluator) {
        FunctionDescriptor descriptor = collectionFunction.binding().descriptor();
        List<Class<?>> parameterTypes = descriptor.parameterTypes();
        List<ExecutableNode> arguments = collectionFunction.arguments();
        Object[] auditArgs = new Object[arguments.size() + 1];
        auditArgs[0] = runtimeServices.coerce(current, parameterTypes.getFirst());
        for (int index = 0; index < arguments.size(); index++) {
            Object value = evaluator.evaluate(arguments.get(index), null);
            auditArgs[index + 1] = runtimeServices.coerce(value, parameterTypes.get(index + 1));
        }
        foldContext.variableReads.add(new AuditEvent.FunctionCall(descriptor.name(), auditArgs, result));
    }

    private boolean isFoldableAccess(ExecutablePropertyChain.ExecutableAccess access) {
        return switch (access) {
            case ExecutablePropertyChain.ExecutableFieldGet ignored -> true;
            case ExecutablePropertyChain.ReflectivePropertyAccess ignored -> true;
            case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke ->
                    methodInvoke.arguments().stream().allMatch(argument -> isFoldableNode(argument, false));
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
                    && collectionFunction.arguments().stream().allMatch(argument -> isFoldableNode(argument, false));
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
            case ExecutablePropertyChain chain ->
                    isFoldablePropertyChainNode(chain, allowFilterContext);
            case ExecutableFunctionCall functionCall ->
                    isFoldableFunctionCall(functionCall, allowFilterContext);
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
                    conditional.conditions().stream().allMatch(condition -> isFoldableNode(condition, allowFilterContext))
                    && conditional.results().stream().allMatch(result -> isFoldableNode(result, allowFilterContext))
                    && isFoldableNode(conditional.elseExpression(), allowFilterContext);
            case ExecutableSimpleConditional conditional ->
                    isFoldableNode(conditional.condition(), allowFilterContext)
                    && isFoldableNode(conditional.thenExpression(), allowFilterContext)
                    && isFoldableNode(conditional.elseExpression(), allowFilterContext);
            case ExecutableVectorLiteral vectorLiteral ->
                    vectorLiteral.isFolded()
                    || vectorLiteral.elements().stream().allMatch(element -> isFoldableNode(element, allowFilterContext));
            case ExecutableNullCoalesce nullCoalesce ->
                    isFoldableNode(nullCoalesce.left(), allowFilterContext)
                    && isFoldableNode(nullCoalesce.right(), allowFilterContext);
            case ExecutableRegexOp regexOp -> isFoldableNode(regexOp.subject(), allowFilterContext);
        };
    }

    private boolean isFoldablePropertyChainNode(ExecutablePropertyChain chain, boolean allowFilterContext) {
        boolean rootIsFoldable = isFoldableNode(chain.root(), allowFilterContext);
        if (!rootIsFoldable) {
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
               && functionCall.arguments().stream().allMatch(argument -> isFoldableNode(argument, allowFilterContext));
    }

    private boolean isLegacyAccessChain(List<PropertyChainNode.MemberAccess> chain) {
        for (PropertyChainNode.MemberAccess access : chain) {
            if (!(access instanceof PropertyChainNode.PropertyAccess)
                    && !(access instanceof PropertyChainNode.SafePropertyAccess)
                    && !(access instanceof PropertyChainNode.MethodCallAccess)
                    && !(access instanceof PropertyChainNode.SafeMethodCallAccess)) {
                return false;
            }
        }
        return true;
    }

    private TypedAccessStep buildTypedAccessStep(
            PropertyChainNode.MemberAccess access,
            List<ExecutableNode> argumentNodes,
            SemanticModel model,
            TypeMetadata metadata,
            TypeHintCatalog typeHintCatalog,
            boolean safe) {
        return switch (access) {
            case PropertyChainNode.PropertyAccess propertyAccess -> {
                PropertyDescriptor descriptor = metadata.properties().get(propertyAccess.name());
                if (descriptor == null) {
                    throw new IllegalStateException("missing property metadata for '" + propertyAccess.name()
                                                    + "' on " + metadata.javaClass().getName());
                }
                yield new TypedAccessStep(
                        new ExecutablePropertyChain.ExecutableFieldGet(
                                propertyAccess.name(),
                                descriptor.getter(),
                                descriptor.resolvedType(),
                                safe
                        ),
                        descriptor.resolvedType()
                );
            }
            case PropertyChainNode.SafePropertyAccess safePropertyAccess -> {
                PropertyDescriptor descriptor = metadata.properties().get(safePropertyAccess.name());
                if (descriptor == null) {
                    throw new IllegalStateException("missing property metadata for '" + safePropertyAccess.name()
                                                    + "' on " + metadata.javaClass().getName());
                }
                yield new TypedAccessStep(
                        new ExecutablePropertyChain.ExecutableFieldGet(
                                safePropertyAccess.name(),
                                descriptor.getter(),
                                descriptor.resolvedType(),
                                true
                        ),
                        descriptor.resolvedType()
                );
            }
            case PropertyChainNode.MethodCallAccess methodCall -> {
                MethodDescriptor descriptor = resolveMethodDescriptor(metadata, methodCall, model, typeHintCatalog);
                yield new TypedAccessStep(
                        new ExecutablePropertyChain.ExecutableMethodInvoke(
                                methodCall.name(),
                                descriptor.handle(),
                                argumentNodes,
                                descriptor.parameterTypes(),
                                descriptor.returnType(),
                                safe
                        ),
                        descriptor.returnType()
                );
            }
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall -> {
                MethodDescriptor descriptor = resolveMethodDescriptor(
                        metadata,
                        safeMethodCall.name(),
                        safeMethodCall.arguments(),
                        model,
                        typeHintCatalog
                );
                yield new TypedAccessStep(
                        new ExecutablePropertyChain.ExecutableMethodInvoke(
                                safeMethodCall.name(),
                                descriptor.handle(),
                                argumentNodes,
                                descriptor.parameterTypes(),
                                descriptor.returnType(),
                                true
                        ),
                        descriptor.returnType()
                );
            }
            default -> throw new IllegalStateException("Unexpected access type in buildTypedAccessStep: " + access);
        };
    }

    private ExecutablePropertyChain.ExecutableAccess buildReflectiveAccess(
            PropertyChainNode.MemberAccess access,
            List<ExecutableNode> argumentNodes,
            boolean safe) {
        return switch (access) {
            case PropertyChainNode.PropertyAccess propertyAccess ->
                    new ExecutablePropertyChain.ReflectivePropertyAccess(propertyAccess.name(), safe);
            case PropertyChainNode.SafePropertyAccess safePropertyAccess ->
                    new ExecutablePropertyChain.ReflectivePropertyAccess(safePropertyAccess.name(), true);
            case PropertyChainNode.MethodCallAccess methodCall ->
                    new ExecutablePropertyChain.ReflectiveMethodInvoke(methodCall.name(), argumentNodes, safe);
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                    new ExecutablePropertyChain.ReflectiveMethodInvoke(safeMethodCall.name(), argumentNodes, true);
            default -> throw new IllegalStateException("Unexpected access type in buildReflectiveAccess: " + access);
        };
    }

    private MethodDescriptor resolveMethodDescriptor(
            TypeMetadata metadata,
            PropertyChainNode.MethodCallAccess methodCall,
            SemanticModel model,
            TypeHintCatalog typeHintCatalog) {
        return resolveMethodDescriptor(
                metadata,
                methodCall.name(),
                methodCall.arguments(),
                model,
                typeHintCatalog
        );
    }

    private MethodDescriptor resolveMethodDescriptor(
            TypeMetadata metadata,
            String methodName,
            List<ExpressionNode> arguments,
            SemanticModel model,
            TypeHintCatalog typeHintCatalog) {
        List<MethodDescriptor> candidates = metadata.methods().get(methodName);
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("missing method metadata for '" + methodName
                                            + "' on " + metadata.javaClass().getName());
        }

        List<MethodDescriptor> arityMatches = candidates.stream()
                .filter(candidate -> candidate.arity() == arguments.size())
                .toList();
        if (arityMatches.isEmpty()) {
            throw new IllegalStateException("missing method overload for '" + methodName
                                            + "' with arity " + arguments.size()
                                            + " on " + metadata.javaClass().getName());
        }

        List<ResolvedType> argumentTypes = arguments.stream()
                .map(argument -> model.findResolvedType(argument.nodeId()).orElse(UnknownType.INSTANCE))
                .toList();

        MethodDescriptor match = null;
        for (MethodDescriptor candidate : arityMatches) {
            if (!matchesMethodArguments(candidate, argumentTypes, typeHintCatalog)) {
                continue;
            }
            if (match != null) {
                throw new IllegalStateException("ambiguous method metadata for '" + methodName
                                                + "' on " + metadata.javaClass().getName());
            }
            match = candidate;
        }
        if (match == null) {
            throw new IllegalStateException("missing compatible method overload for '" + methodName
                                            + "' on " + metadata.javaClass().getName());
        }
        return match;
    }

    private record TypedAccessStep(
            ExecutablePropertyChain.ExecutableAccess access,
            ResolvedType nextType
    ) {
    }

    private boolean matchesMethodArguments(
            MethodDescriptor descriptor,
            List<ResolvedType> argumentTypes,
            TypeHintCatalog typeHintCatalog) {
        for (int index = 0; index < argumentTypes.size(); index++) {
            ResolvedType actualType = argumentTypes.get(index);
            ResolvedType expectedType = resolveJavaType(descriptor.parameterTypes().get(index), typeHintCatalog);
            if (actualType != UnknownType.INSTANCE
                && expectedType != UnknownType.INSTANCE
                && !actualType.equals(expectedType)) {
                return false;
            }
        }
        return true;
    }

    private ResolvedType resolveRootType(
            SymbolRef root,
            SemanticModel model,
            ExternalSymbolCatalog externalSymbolCatalog) {
        if (root.kind() == SymbolKind.EXTERNAL) {
            ExternalSymbolDescriptor descriptor = externalSymbolCatalog.findOrNull(root.name());
            return descriptor == null ? UnknownType.INSTANCE : descriptor.declaredType();
        }
        return resolveInternalSymbolType(root, model);
    }

    private ResolvedType resolveInternalSymbolType(SymbolRef root, SemanticModel model) {
        List<AssignmentNode> assignments = model.assignmentUsages().get(root);
        if (assignments == null || assignments.isEmpty()) {
            return UnknownType.INSTANCE;
        }
        ResolvedType resolvedType = UnknownType.INSTANCE;
        for (AssignmentNode assignment : assignments) {
            if (assignment instanceof DestructuringAssignmentNode) {
                return UnknownType.INSTANCE;
            }
            resolvedType = ResolvedTypes.merge(
                    resolvedType,
                    model.findResolvedType(assignment.nodeId()).orElse(UnknownType.INSTANCE)
            );
        }
        return resolvedType;
    }

    private ResolvedType resolveJavaType(Class<?> javaType, TypeHintCatalog typeHintCatalog) {
        return typeHintCatalog.isRegistered(javaType)
                ? new ObjectType(javaType)
                : ResolvedTypes.fromJavaType(javaType);
    }

    private ExecutableNode buildLiteral(LiteralNode lit, SemanticModel model) {
        String text = lit.value();
        return switch (text) {
            case "currDate" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_DATE);
            case "currTime" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_TIME);
            case "currDateTime" -> new ExecutableDynamicLiteral(DynamicInstant.CURR_DATETIME);
            default -> {
                ResolvedType resolvedType = model.findResolvedType(lit.nodeId())
                        .orElseThrow(() -> new IllegalStateException(
                                "missing resolved type for literal '" + text + "'"));
                yield new ExecutableLiteral(materialize(text, resolvedType));
            }
        };
    }

    private Object materialize(String text, ResolvedType resolvedType) {
        if (resolvedType == NullType.INSTANCE) return null;
        if (resolvedType == ScalarType.NUMBER) return new BigDecimal(text);
        if (resolvedType == ScalarType.BOOLEAN) return Boolean.parseBoolean(text);
        if (resolvedType == ScalarType.STRING) return unquote(text);
        if (resolvedType == ScalarType.DATE) return LocalDate.parse(text);
        if (resolvedType == ScalarType.TIME) return LocalTime.parse(text);
        if (resolvedType == ScalarType.DATETIME) {
            return text.contains("+") || text.endsWith("Z")
                    ? OffsetDateTime.parse(text).toLocalDateTime()
                    : LocalDateTime.parse(text);
        }
        throw new IllegalStateException("unsupported literal type: " + resolvedType);
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
        if (descriptor.isFoldable() && arguments.stream().allMatch(this::isConstantNode)) {
            int arity = descriptor.arity();
            Object[] args = new Object[arity];
            for (int i = 0; i < arity; i++) {
                args[i] = runtimeServices.coerce(
                        constantValue(arguments.get(i)),
                        descriptor.parameterTypes().get(i));
            }
            Object result = descriptor.invoke(args);
            return ExecutableFunctionCall.folded(binding, arguments, args, result);
        }

        return ExecutableFunctionCall.of(binding, arguments);
    }

    private boolean isConstantNode(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> true;
            case ExecutableFunctionCall f -> f.isFolded();
            case ExecutableVectorLiteral v -> v.isFolded();
            default -> false;
        };
    }

    private Object constantValue(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral lit -> lit.precomputed();
            case ExecutableFunctionCall f -> f.foldedResult();
            case ExecutableVectorLiteral v -> v.foldedValue();
            default -> throw new IllegalStateException("not a constant node: " + node);
        };
    }

    private final class ConstantNodeEvaluator implements NodeEvaluator {

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

    private String unquote(String value) {
        if (value.length() < 2) {
            return value;
        }
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return value;
    }

    private static final class FoldContext {
        final Map<SymbolRef, Object> symbols = new HashMap<>();
        final List<AuditEvent> variableReads = new ArrayList<>();
    }
}
