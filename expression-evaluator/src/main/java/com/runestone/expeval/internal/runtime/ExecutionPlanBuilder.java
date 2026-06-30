package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.catalog.*;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.*;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.navigation.NavigationMode;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;
import com.runestone.expeval.internal.semantic.MemberBindingKey;
import com.runestone.expeval.internal.semantic.ResolvedFunctionBinding;
import com.runestone.expeval.internal.semantic.ResolvedMemberBinding;
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

    ExecutionPlan build(
            SemanticModel model,
            Map<MemberBindingKey, ResolvedMemberBinding> memberBindings,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            MathContext mathContext) {
        // 1. Assign indices to symbols
        assignIndices(model);

        ExpressionFileNode ast = model.ast();
        // Process assignments sequentially so each one can propagate its constant value
        // to later assignments and to the result expression.
        ConstantFoldingPolicy foldingPolicy = new ConstantFoldingPolicy(runtimeServices, mathContext);
        foldingPolicy.seedExternalConstants(model, externalSymbolCatalog);

        List<ExecutableAssignment> assignments = new ArrayList<>();
        for (AssignmentNode assignment : ast.assignments()) {
            assignments.add(buildAssignment(assignment, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy));
        }
        ExecutableNode resultNode = ast.resultExpression() != null
                ? buildNode(ast.resultExpression(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy)
                : null;
        List<AuditEvent> foldedVariableReads = foldingPolicy.foldedVariableReads();
        int maxAuditEvents = countMaxAuditEvents(assignments, resultNode) + foldedVariableReads.size();

        int externalSymbolsCount = model.externalSymbolsByName().size();
        Object[] defaults = seedDefaults(model, externalSymbolCatalog, runtimeServices, externalSymbolsCount);
        Map<String, ExternalBindingPlan> externalBindingPlans = seedExternalBindingPlans(model, externalSymbolCatalog);
        boolean containsDynamicInstant = containsDynamicInstant(assignments, resultNode);

        return new ExecutionPlan(assignments, resultNode, defaults, externalBindingPlans, externalSymbolsCount, maxAuditEvents,
                foldedVariableReads, containsDynamicInstant);
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

    private boolean containsDynamicInstant(List<ExecutableAssignment> assignments, ExecutableNode resultExpression) {
        for (ExecutableAssignment assignment : assignments) {
            ExecutableNode value = switch (assignment) {
                case ExecutableSimpleAssignment simple -> simple.value();
                case ExecutableDestructuringAssignment destructuring -> destructuring.value();
            };
            if (containsDynamicInstant(value)) {
                return true;
            }
        }
        return resultExpression != null && containsDynamicInstant(resultExpression);
    }

    private boolean containsDynamicInstant(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> false;
            case ExecutableDynamicLiteral ignored -> true;
            case ExecutableIdentifier ignored -> false;
            case ExecutablePropertyChain chain -> containsDynamicInstant(chain);
            case ExecutableFunctionCall functionCall -> containsDynamicInstant(functionCall.arguments());
            case ExecutableBinaryOp binaryOp -> containsDynamicInstant(binaryOp.left()) || containsDynamicInstant(binaryOp.right());
            case ExecutableTernaryOp ternaryOp -> containsDynamicInstant(ternaryOp.first())
                    || containsDynamicInstant(ternaryOp.second())
                    || containsDynamicInstant(ternaryOp.third());
            case ExecutableUnaryOp unaryOp -> containsDynamicInstant(unaryOp.operand());
            case ExecutablePostfixOp postfixOp -> containsDynamicInstant(postfixOp.operand());
            case ExecutableConditional conditional -> containsDynamicInstant(conditional.conditions())
                    || containsDynamicInstant(conditional.results())
                    || containsDynamicInstant(conditional.elseExpression());
            case ExecutableSimpleConditional conditional -> containsDynamicInstant(conditional.condition())
                    || containsDynamicInstant(conditional.thenExpression())
                    || containsDynamicInstant(conditional.elseExpression());
            case ExecutableVectorLiteral vectorLiteral -> containsDynamicInstant(vectorLiteral.elements());
            case ExecutableNullCoalesce nullCoalesce -> containsDynamicInstant(nullCoalesce.left())
                    || containsDynamicInstant(nullCoalesce.right());
            case ExecutableRegexOp regexOp -> containsDynamicInstant(regexOp.subject());
        };
    }

    private boolean containsDynamicInstant(List<ExecutableNode> nodes) {
        for (ExecutableNode node : nodes) {
            if (containsDynamicInstant(node)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsDynamicInstant(ExecutablePropertyChain chain) {
        if (containsDynamicInstant(chain.root())) {
            return true;
        }
        for (ExecutablePropertyChain.ExecutableAccess access : chain.chain()) {
            if (containsDynamicInstant(access)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsDynamicInstant(ExecutablePropertyChain.ExecutableAccess access) {
        return switch (access) {
            case ExecutablePropertyChain.ExecutableFieldGet ignored -> false;
            case ExecutablePropertyChain.ReflectivePropertyAccess ignored -> false;
            case ExecutablePropertyChain.ExecutableMethodInvoke methodInvoke -> containsDynamicInstant(methodInvoke.arguments());
            case ExecutablePropertyChain.ReflectiveMethodInvoke methodInvoke -> containsDynamicInstant(methodInvoke.arguments());
            case ExecutablePropertyChain.ExecutableIndexAccess indexAccess -> containsDynamicInstant(indexAccess.index());
            case ExecutablePropertyChain.ExecutableMapKeyAccess ignored -> false;
            case ExecutablePropertyChain.ExecutableSliceAccess sliceAccess -> containsDynamicInstantOrFalse(sliceAccess.start())
                    || containsDynamicInstantOrFalse(sliceAccess.end());
            case ExecutablePropertyChain.ExecutableWildcard ignored -> false;
            case ExecutablePropertyChain.ExecutableFilterPredicate filterPredicate -> containsDynamicInstant(filterPredicate.predicate());
            case ExecutablePropertyChain.ExecutableDeepScan ignored -> false;
            case ExecutablePropertyChain.ExecutableCollectionFunction collectionFunction -> containsDynamicInstant(collectionFunction.arguments());
            case ExecutablePropertyChain.ExecutableMapProjection ignored -> false;
            case ExecutablePropertyChain.ExecutableVectorMap vectorMap -> containsDynamicInstant(vectorMap.transform());
            case ExecutablePropertyChain.ExecutableVectorAggregation aggregation -> containsDynamicInstantOrFalse(aggregation.transform());
        };
    }

    private boolean containsDynamicInstantOrFalse(ExecutableNode node) {
        return node != null && containsDynamicInstant(node);
    }

    private ExecutableAssignment buildAssignment(
            AssignmentNode assignment,
            SemanticModel model,
            Map<MemberBindingKey, ResolvedMemberBinding> memberBindings,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            MathContext mathContext,
            ConstantFoldingPolicy foldingPolicy) {
        return switch (assignment) {
            case SimpleAssignmentNode s -> {
                SymbolRef target = model.internalSymbolsByName().get(s.targetName());
                ExecutableNode value = buildNode(s.value(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                foldingPolicy.publishAssignment(target, value);
                yield new ExecutableSimpleAssignment(target, value);
            }
            case DestructuringAssignmentNode d -> {
                List<SymbolRef> targets = d.targetNames().stream()
                        .map(name -> model.internalSymbolsByName().get(name))
                        .toList();
                yield new ExecutableDestructuringAssignment(
                        targets,
                        buildNode(d.value(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy)
                );
            }
        };
    }

    private ExecutableNode buildNode(
            ExpressionNode node,
            SemanticModel model,
            Map<MemberBindingKey, ResolvedMemberBinding> memberBindings,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            MathContext mathContext,
            ConstantFoldingPolicy foldingPolicy) {
        return switch (node) {
            case LiteralNode lit -> buildLiteral(lit, model);
            case IdentifierNode id -> buildIdentifier(id, model, foldingPolicy);
            case PropertyChainNode chain -> buildPropertyChain(
                    chain, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
            case FunctionCallNode f ->
                    buildFunctionCall(f, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
            case BinaryOperationNode b when b.operator() == BinaryOperator.NULL_COALESCE -> {
                ExecutableNode left = buildNode(b.left(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                if (foldingPolicy.isConstantNode(left)) {
                    Object leftVal = foldingPolicy.constantValue(left);
                    if (leftVal != null) {
                        yield new ExecutableLiteral(leftVal);
                    }
                }
                yield new ExecutableNullCoalesce(
                        left,
                        buildNode(b.right(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy)
                );
            }
            case BinaryOperationNode b when b.operator() == BinaryOperator.REGEX_MATCH
                                         || b.operator() == BinaryOperator.REGEX_NOT_MATCH -> {
                boolean negate = b.operator() == BinaryOperator.REGEX_NOT_MATCH;
                ExecutableNode subjectNode = buildNode(b.left(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                LiteralNode patternLit = (LiteralNode) b.right();
                yield new ExecutableRegexOp(subjectNode, Pattern.compile(unquote(patternLit.value())), negate);
            }
            case BinaryOperationNode b -> {
                ExecutableNode left = buildNode(b.left(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                ExecutableNode right = buildNode(b.right(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                if (foldingPolicy.isConstantNode(left) && foldingPolicy.isConstantNode(right)) {
                    Object leftVal = foldingPolicy.constantValue(left);
                    Object rightVal = foldingPolicy.constantValue(right);
                    Object result = OperatorEvaluator.evaluateBinary(b.operator(), leftVal, rightVal, runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableBinaryOp(b.operator(), left, right);
            }
            case TernaryOperationNode t -> {
                ExecutableNode first = buildNode(t.first(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                ExecutableNode second = buildNode(t.second(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                ExecutableNode third = buildNode(t.third(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                if (foldingPolicy.isConstantNode(first) && foldingPolicy.isConstantNode(second) && foldingPolicy.isConstantNode(third)) {
                    Object result = OperatorEvaluator.evaluateTernary(t.operator(), foldingPolicy.constantValue(first), foldingPolicy.constantValue(second), foldingPolicy.constantValue(third), runtimeServices);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableTernaryOp(t.operator(), first, second, third);
            }
            case UnaryOperationNode u -> {
                ExecutableNode operand = buildNode(u.operand(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                if (foldingPolicy.isConstantNode(operand)) {
                    Object result = OperatorEvaluator.evaluateUnary(u.operator(), foldingPolicy.constantValue(operand), runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutableUnaryOp(u.operator(), operand);
            }
            case PostfixOperationNode p -> {
                ExecutableNode operand = buildNode(p.operand(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                if (foldingPolicy.isConstantNode(operand)) {
                    Object result = OperatorEvaluator.evaluatePostfix(p.operator(), foldingPolicy.constantValue(operand), runtimeServices, mathContext);
                    yield new ExecutableLiteral(result);
                }
                yield new ExecutablePostfixOp(p.operator(), operand);
            }
            case ConditionalNode c -> {
                List<ExecutableNode> conditions = new ArrayList<>();
                List<ExecutableNode> results = new ArrayList<>();
                ExecutableNode elseExpr = null;

                for (int i = 0; i < c.conditions().size(); i++) {
                    ExecutableNode condNode = buildNode(c.conditions().get(i), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                    if (foldingPolicy.isConstantNode(condNode)) {
                        Object val = foldingPolicy.constantValue(condNode);
                        if (Boolean.TRUE.equals(val)) {
                            elseExpr = buildNode(c.results().get(i), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                            break;
                        }
                        // If constant false, just skip this branch
                    } else {
                        conditions.add(condNode);
                        results.add(buildNode(c.results().get(i), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy));
                    }
                }

                if (elseExpr == null) {
                    elseExpr = buildNode(c.elseExpression(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
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
                        .map(e -> buildNode(e, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy))
                        .toList();
                yield foldingPolicy.vectorLiteral(elements);
            }
        };
    }

    private ExecutableNode buildPropertyChain(
            PropertyChainNode node,
            SemanticModel model,
            Map<MemberBindingKey, ResolvedMemberBinding> memberBindings,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            MathContext mathContext,
            ConstantFoldingPolicy foldingPolicy) {

        // '@' root has no external symbol binding; create a special sentinel identifier.
        ExecutableNode root;
        if (LanguageSymbols.CURRENT_ELEMENT.equals(node.rootIdentifier())) {
            root = new ExecutableIdentifier(new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL), node.sourceSpan());
        } else {
            SymbolRef rootRef = model.findSymbol(node.nodeId())
                    .orElseThrow(() -> new IllegalStateException(
                            "missing symbol for property chain '" + node.rootIdentifier() + "'"));
            root = foldingPolicy.foldedSymbolReadOrNull(rootRef);
            if (root == null) {
                root = new ExecutableIdentifier(rootRef, node.sourceSpan());
            }
        }

        if (isLegacyAccessChain(node.chain())) {
            return buildLegacyPropertyChain(
                    node,
                    model,
                    memberBindings,
                    runtimeServices,
                    externalSymbolCatalog,
                    mathContext,
                    foldingPolicy,
                    root
            );
        }

        List<ExecutablePropertyChain.ExecutableAccess> steps = new ArrayList<>(node.chain().size());

        for (int accessIndex = 0; accessIndex < node.chain().size(); accessIndex++) {
            PropertyChainNode.MemberAccess access = node.chain().get(accessIndex);
            boolean safe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;

            // ----- New navigation step types (take priority over typed/reflective paths) -----
            switch (access) {
                case PropertyChainNode.CollectionIndexStep cis -> {
                    ExecutableNode idx = buildNode(cis.index(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                    steps.add(new ExecutablePropertyChain.ExecutableIndexAccess(idx));
                    continue;
                }
                case PropertyChainNode.MapKeyStep mks -> {
                    steps.add(new ExecutablePropertyChain.ExecutableMapKeyAccess(mks.key()));
                    continue;
                }
                case PropertyChainNode.CollectionSliceStep css -> {
                    ExecutableNode start = css.start() != null
                            ? buildNode(css.start(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy)
                            : null;
                    ExecutableNode end = css.end() != null
                            ? buildNode(css.end(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy)
                            : null;
                    steps.add(new ExecutablePropertyChain.ExecutableSliceAccess(start, end));
                    continue;
                }
                case PropertyChainNode.WildcardStep ignored -> {
                    steps.add(new ExecutablePropertyChain.ExecutableWildcard());
                    continue;
                }
                case PropertyChainNode.FilterPredicateStep fps -> {
                    ExecutableNode predicate = buildNode(fps.predicate(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                    steps.add(new ExecutablePropertyChain.ExecutableFilterPredicate(predicate));
                    continue;
                }
                case PropertyChainNode.DeepScanStep dss -> {
                    steps.add(new ExecutablePropertyChain.ExecutableDeepScan(dss.propertyName()));
                    continue;
                }
                case PropertyChainNode.CollectionFunctionStep cfs -> {
                    List<ExecutableNode> args = cfs.arguments().stream()
                            .map(a -> buildNode(a, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy))
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
                    continue;
                }
                case PropertyChainNode.MapProjectionStep mps -> {
                    steps.add(new ExecutablePropertyChain.ExecutableMapProjection(mps.kind()));
                    continue;
                }
                case PropertyChainNode.VectorMapStep vms -> {
                    ExecutableNode execTransform = buildNode(vms.transform(), model, memberBindings, runtimeServices,
                            externalSymbolCatalog, mathContext, foldingPolicy);
                    steps.add(new ExecutablePropertyChain.ExecutableVectorMap(execTransform));
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
                            : buildNode(vas.transform(), model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy);
                    steps.add(new ExecutablePropertyChain.ExecutableVectorAggregation(vas.kind(), execTransform));
                    continue;
                }
                default -> { /* fall through to typed/reflective path below */ }
            }

            // ----- Existing typed / reflective access -----
            List<ExecutableNode> argumentNodes = switch (access) {
                case PropertyChainNode.MethodCallAccess methodCall ->
                        methodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy))
                                .toList();
                case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                        safeMethodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy))
                                .toList();
                default -> List.of();
            };

            ResolvedMemberBinding binding = memberBindings.get(new MemberBindingKey(node.nodeId(), accessIndex));
            if (binding != null) {
                steps.add(buildBoundAccess(binding, argumentNodes));
                continue;
            }

            steps.add(buildReflectiveAccess(access, argumentNodes, safe));
        }
        return foldingPolicy.foldPropertyChainPrefix(root, steps);
    }

    private ExecutableNode buildLegacyPropertyChain(
            PropertyChainNode node,
            SemanticModel model,
            Map<MemberBindingKey, ResolvedMemberBinding> memberBindings,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            MathContext mathContext,
            ConstantFoldingPolicy foldingPolicy,
            ExecutableNode root) {
        List<ExecutablePropertyChain.ExecutableAccess> steps = new ArrayList<>(node.chain().size());

        for (int accessIndex = 0; accessIndex < node.chain().size(); accessIndex++) {
            PropertyChainNode.MemberAccess access = node.chain().get(accessIndex);
            boolean safe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;
            List<ExecutableNode> argumentNodes = switch (access) {
                case PropertyChainNode.MethodCallAccess methodCall ->
                        methodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy))
                                .toList();
                case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                        safeMethodCall.arguments().stream()
                                .map(argument -> buildNode(argument, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy))
                                .toList();
                default -> List.of();
            };

            ResolvedMemberBinding binding = memberBindings.get(new MemberBindingKey(node.nodeId(), accessIndex));
            if (binding != null) {
                steps.add(buildBoundAccess(binding, argumentNodes));
                continue;
            }

            steps.add(buildReflectiveAccess(access, argumentNodes, safe));
        }
        return foldingPolicy.foldPropertyChainPrefix(root, steps);
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

    private ExecutablePropertyChain.ExecutableAccess buildBoundAccess(
            ResolvedMemberBinding binding,
            List<ExecutableNode> argumentNodes) {
        return switch (binding.kind()) {
            case PROPERTY -> new ExecutablePropertyChain.ExecutableFieldGet(
                    binding.name(),
                    binding.handle(),
                    binding.returnType(),
                    binding.safe());
            case METHOD -> new ExecutablePropertyChain.ExecutableMethodInvoke(
                    binding.name(),
                    binding.handle(),
                    argumentNodes,
                    binding.parameterTypes(),
                    binding.returnType(),
                    binding.safe());
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

    private ExecutableNode buildIdentifier(IdentifierNode id, SemanticModel model, ConstantFoldingPolicy foldingPolicy) {
        // '@' is the filter-predicate current-element sentinel — no model symbol, always dynamic
        if (LanguageSymbols.CURRENT_ELEMENT.equals(id.name())) {
            return new ExecutableIdentifier(new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL), id.sourceSpan());
        }
        SymbolRef ref = model.findSymbol(id.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing symbol for identifier '" + id.name() + "'"));
        ExecutableNode folded = foldingPolicy.foldedSymbolReadOrNull(ref);
        if (folded != null) {
            return folded;
        }
        return new ExecutableIdentifier(ref, id.sourceSpan());
    }

    private ExecutableNode buildFunctionCall(
            FunctionCallNode f,
            SemanticModel model,
            Map<MemberBindingKey, ResolvedMemberBinding> memberBindings,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            MathContext mathContext,
            ConstantFoldingPolicy foldingPolicy) {
        ResolvedFunctionBinding binding = model.findFunctionBinding(f.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing function binding for '" + f.functionName() + "'"));
        List<ExecutableNode> arguments = f.arguments().stream()
                .map(arg -> buildNode(arg, model, memberBindings, runtimeServices, externalSymbolCatalog, mathContext, foldingPolicy))
                .toList();
        return foldingPolicy.functionCall(binding, arguments);
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
