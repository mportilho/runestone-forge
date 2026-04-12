package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.catalog.*;
import com.runestone.expeval.internal.ast.*;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.navigation.NavigationMode;
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
        Map<SymbolRef, Object> foldedSymbols = new HashMap<>();

        model.externalSymbolsByName().forEach((name, symbolRef) -> {
            ExternalSymbolDescriptor descriptor = externalSymbolCatalog.findOrNull(name);
            if (descriptor != null && !descriptor.overridable()) {
                foldedSymbols.put(symbolRef, runtimeServices.coerceToResolvedType(descriptor.defaultValue(), descriptor.declaredType()));
            }
        });

        List<ExecutableAssignment> assignments = new ArrayList<>();
        for (AssignmentNode assignment : ast.assignments()) {
            assignments.add(buildAssignment(assignment, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols));
        }
        ExecutableNode resultNode = ast.resultExpression() != null
                ? buildNode(ast.resultExpression(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols)
                : null;
        int maxAuditEvents = countMaxAuditEvents(assignments, resultNode);

        int externalSymbolsCount = model.externalSymbolsByName().size();
        Object[] defaults = seedDefaults(model, externalSymbolCatalog, runtimeServices, externalSymbolsCount);
        Map<String, ExternalBindingPlan> externalBindingPlans = seedExternalBindingPlans(model, externalSymbolCatalog);

        return new ExecutionPlan(assignments, resultNode, defaults, externalBindingPlans, externalSymbolsCount, maxAuditEvents);
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
            Map<SymbolRef, Object> foldedSymbols) {
        return switch (assignment) {
            case SimpleAssignmentNode s -> {
                SymbolRef target = model.internalSymbolsByName().get(s.targetName());
                ExecutableNode value = buildNode(s.value(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                if (isConstantNode(value)) {
                    foldedSymbols.put(target, constantValue(value));
                } else {
                    foldedSymbols.remove(target);
                }
                yield new ExecutableSimpleAssignment(target, value);
            }
            case DestructuringAssignmentNode d -> {
                List<SymbolRef> targets = d.targetNames().stream()
                        .map(name -> model.internalSymbolsByName().get(name))
                        .toList();
                yield new ExecutableDestructuringAssignment(
                        targets,
                        buildNode(d.value(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols)
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
            Map<SymbolRef, Object> foldedSymbols) {
        return switch (node) {
            case LiteralNode lit -> buildLiteral(lit, model);
            case IdentifierNode id -> buildIdentifier(id, model, foldedSymbols);
            case PropertyChainNode chain -> buildPropertyChain(
                    chain, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
            case FunctionCallNode f ->
                    buildFunctionCall(f, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
            case BinaryOperationNode b when b.operator() == BinaryOperator.NULL_COALESCE -> {
                ExecutableNode left = buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                if (isConstantNode(left)) {
                    Object leftVal = constantValue(left);
                    if (leftVal != null) {
                        yield new ExecutableLiteral(leftVal);
                    }
                }
                yield new ExecutableNullCoalesce(
                        left,
                        buildNode(b.right(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols)
                );
            }
            case BinaryOperationNode b when b.operator() == BinaryOperator.REGEX_MATCH
                                         || b.operator() == BinaryOperator.REGEX_NOT_MATCH -> {
                boolean negate = b.operator() == BinaryOperator.REGEX_NOT_MATCH;
                ExecutableNode subjectNode = buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                LiteralNode patternLit = (LiteralNode) b.right();
                yield new ExecutableRegexOp(subjectNode, Pattern.compile(unquote(patternLit.value())), negate);
            }
            case BinaryOperationNode b -> {
                ExecutableNode left = buildNode(b.left(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                ExecutableNode right = buildNode(b.right(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                if (isConstantNode(left) && isConstantNode(right)) {
                    Object leftVal = constantValue(left);
                    Object rightVal = constantValue(right);
                    Object result = OperatorEvaluator.evaluateBinary(b.operator(), leftVal, rightVal, runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableBinaryOp(b.operator(), left, right);
            }
            case TernaryOperationNode t -> {
                ExecutableNode first = buildNode(t.first(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                ExecutableNode second = buildNode(t.second(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                ExecutableNode third = buildNode(t.third(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                if (isConstantNode(first) && isConstantNode(second) && isConstantNode(third)) {
                    Object result = OperatorEvaluator.evaluateTernary(t.operator(), constantValue(first), constantValue(second), constantValue(third), runtimeServices);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableTernaryOp(t.operator(), first, second, third);
            }
            case UnaryOperationNode u -> {
                ExecutableNode operand = buildNode(u.operand(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                if (isConstantNode(operand)) {
                    Object result = OperatorEvaluator.evaluateUnary(u.operator(), constantValue(operand), runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableUnaryOp(u.operator(), operand);
            }
            case PostfixOperationNode p -> {
                ExecutableNode operand = buildNode(p.operand(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
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
                    ExecutableNode condNode = buildNode(c.conditions().get(i), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                    if (isConstantNode(condNode)) {
                        Object val = constantValue(condNode);
                        if (Boolean.TRUE.equals(val)) {
                            elseExpr = buildNode(c.results().get(i), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
                            break;
                        }
                        // If constant false, just skip this branch
                    } else {
                        conditions.add(condNode);
                        results.add(buildNode(c.results().get(i), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols));
                    }
                }

                if (elseExpr == null) {
                    elseExpr = buildNode(c.elseExpression(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
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
                        .map(e -> buildNode(e, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols))
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
            Map<SymbolRef, Object> foldedSymbols) {

        // '@' root has no external symbol binding; create a special sentinel identifier.
        ExecutableNode root;
        if ("@".equals(node.rootIdentifier())) {
            root = new ExecutableIdentifier(new SymbolRef("@", SymbolKind.EXTERNAL), node.sourceSpan());
        } else {
            SymbolRef rootRef = model.findSymbol(node.nodeId())
                    .orElseThrow(() -> new IllegalStateException(
                            "missing symbol for property chain '" + node.rootIdentifier() + "'"));
            if (foldedSymbols.containsKey(rootRef)) {
                root = new ExecutableLiteral(foldedSymbols.get(rootRef));
            } else {
                root = new ExecutableIdentifier(rootRef, node.sourceSpan());
            }
        }

        ResolvedType currentType = "@".equals(node.rootIdentifier())
                ? UnknownType.INSTANCE
                : resolveRootType(
                        model.findSymbol(node.nodeId()).orElse(new SymbolRef("@", SymbolKind.EXTERNAL)),
                        model, externalSymbolCatalog);

        if (isLegacyAccessChain(node.chain())) {
            return buildLegacyPropertyChain(
                    node,
                    model,
                    runtimeServices,
                    externalSymbolCatalog,
                    typeHintCatalog,
                    mathContext,
                    foldedSymbols,
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
                    ExecutableNode idx = buildNode(cis.index(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
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
                            ? buildNode(css.start(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols)
                            : null;
                    ExecutableNode end = css.end() != null
                            ? buildNode(css.end(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols)
                            : null;
                    steps.add(new ExecutablePropertyChain.ExecutableSliceAccess(start, end));
                    continue;
                }
                case PropertyChainNode.WildcardStep ignored -> {
                    steps.add(new ExecutablePropertyChain.ExecutableWildcard());
                    continue;
                }
                case PropertyChainNode.FilterPredicateStep fps -> {
                    ExecutableNode predicate = buildNode(fps.predicate(), model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols);
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
                            .map(a -> buildNode(a, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols))
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
                case PropertyChainNode.VectorAggregationStep vas -> {
                    steps.add(new ExecutablePropertyChain.ExecutableVectorAggregation(vas.kind()));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                default -> { /* fall through to typed/reflective path below */ }
            }

            // ----- Existing typed / reflective access -----
            List<ExecutableNode> argumentNodes = switch (access) {
                case PropertyChainNode.MethodCallAccess methodCall ->
                        methodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols))
                                .toList();
                case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                        safeMethodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols))
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
        return new ExecutablePropertyChain(root, steps);
    }

    private ExecutableNode buildLegacyPropertyChain(
            PropertyChainNode node,
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext,
            Map<SymbolRef, Object> foldedSymbols,
            ExecutableNode root,
            ResolvedType currentType) {
        List<ExecutablePropertyChain.ExecutableAccess> steps = new ArrayList<>(node.chain().size());

        for (PropertyChainNode.MemberAccess access : node.chain()) {
            boolean safe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;
            List<ExecutableNode> argumentNodes = switch (access) {
                case PropertyChainNode.MethodCallAccess methodCall ->
                        methodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols))
                                .toList();
                case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                        safeMethodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols))
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
        return new ExecutablePropertyChain(root, steps);
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

    private ExecutableNode buildIdentifier(IdentifierNode id, SemanticModel model, Map<SymbolRef, Object> foldedSymbols) {
        // '@' is the filter-predicate current-element sentinel — no model symbol, always dynamic
        if ("@".equals(id.name())) {
            return new ExecutableIdentifier(new SymbolRef("@", SymbolKind.EXTERNAL), id.sourceSpan());
        }
        SymbolRef ref = model.findSymbol(id.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing symbol for identifier '" + id.name() + "'"));
        if (foldedSymbols.containsKey(ref)) {
            return new ExecutableLiteral(foldedSymbols.get(ref));
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
            Map<SymbolRef, Object> foldedSymbols) {
        ResolvedFunctionBinding binding = model.findFunctionBinding(f.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing function binding for '" + f.functionName() + "'"));
        List<ExecutableNode> arguments = f.arguments().stream()
                .map(arg -> buildNode(arg, model, runtimeServices, externalSymbolCatalog, typeHintCatalog, mathContext, foldedSymbols))
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
}
