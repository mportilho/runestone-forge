package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.TypeHintCatalog;
import com.runestone.expeval.catalog.TypeMetadata;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.AssignmentNode;
import com.runestone.expeval.internal.ast.BinaryOperationNode;
import com.runestone.expeval.internal.ast.BinaryOperator;
import com.runestone.expeval.internal.ast.ConditionalNode;
import com.runestone.expeval.internal.ast.DestructuringAssignmentNode;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.FunctionCallNode;
import com.runestone.expeval.internal.ast.IdentifierNode;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.PostfixOperationNode;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.internal.ast.SimpleAssignmentNode;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.ast.UnaryOperationNode;
import com.runestone.expeval.internal.ast.VectorLiteralNode;
import com.runestone.expeval.internal.navigation.NavigationMode;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;
import com.runestone.expeval.types.ObjectType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.UnknownType;
import com.runestone.expeval.types.VectorType;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class ExecutableNodeBuilder {

    private final SemanticModel model;
    private final RuntimeServices runtimeServices;
    private final ExternalSymbolCatalog externalSymbolCatalog;
    private final TypeHintCatalog typeHintCatalog;
    private final MathContext mathContext;
    private final ConstantFoldContext foldContext;

    ExecutableNodeBuilder(
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext,
            ConstantFoldContext foldContext) {
        this.model = model;
        this.runtimeServices = runtimeServices;
        this.externalSymbolCatalog = externalSymbolCatalog;
        this.typeHintCatalog = typeHintCatalog;
        this.mathContext = mathContext;
        this.foldContext = foldContext;
    }

    ExecutableAssignment buildAssignment(AssignmentNode assignment) {
        return switch (assignment) {
            case SimpleAssignmentNode simpleAssignment -> {
                SymbolRef target = model.internalSymbolsByName().get(simpleAssignment.targetName());
                ExecutableNode value = buildNode(simpleAssignment.value());
                if (ConstantNodeValues.isConstant(value)) {
                    foldContext.symbols.put(target, ConstantNodeValues.value(value));
                } else {
                    foldContext.symbols.remove(target);
                }
                yield new ExecutableSimpleAssignment(target, value);
            }
            case DestructuringAssignmentNode destructuringAssignment -> {
                List<SymbolRef> targets = destructuringAssignment.targetNames().stream()
                        .map(name -> model.internalSymbolsByName().get(name))
                        .toList();
                yield new ExecutableDestructuringAssignment(targets, buildNode(destructuringAssignment.value()));
            }
        };
    }

    ExecutableNode buildNode(ExpressionNode node) {
        return switch (node) {
            case LiteralNode literal -> LiteralMaterializer.build(literal, model);
            case IdentifierNode identifier -> buildIdentifier(identifier);
            case PropertyChainNode chain -> buildPropertyChain(chain);
            case FunctionCallNode functionCall -> buildFunctionCall(functionCall);
            case BinaryOperationNode binaryOperation when binaryOperation.operator() == BinaryOperator.NULL_COALESCE ->
                    buildNullCoalesce(binaryOperation);
            case BinaryOperationNode binaryOperation when binaryOperation.operator() == BinaryOperator.REGEX_MATCH
                                                   || binaryOperation.operator() == BinaryOperator.REGEX_NOT_MATCH ->
                    buildRegexOperation(binaryOperation);
            case BinaryOperationNode binaryOperation -> buildBinaryOperation(binaryOperation);
            case TernaryOperationNode ternaryOperation -> buildTernaryOperation(ternaryOperation);
            case UnaryOperationNode unaryOperation -> buildUnaryOperation(unaryOperation);
            case PostfixOperationNode postfixOperation -> buildPostfixOperation(postfixOperation);
            case ConditionalNode conditional -> buildConditional(conditional);
            case VectorLiteralNode vectorLiteral -> buildVectorLiteral(vectorLiteral);
        };
    }

    private ExecutableNode buildNullCoalesce(BinaryOperationNode binaryOperation) {
        ExecutableNode left = buildNode(binaryOperation.left());
        if (ConstantNodeValues.isConstant(left)) {
            Object leftValue = ConstantNodeValues.value(left);
            if (leftValue != null) {
                return new ExecutableLiteral(leftValue);
            }
        }
        return new ExecutableNullCoalesce(left, buildNode(binaryOperation.right()));
    }

    private ExecutableNode buildRegexOperation(BinaryOperationNode binaryOperation) {
        boolean negate = binaryOperation.operator() == BinaryOperator.REGEX_NOT_MATCH;
        ExecutableNode subjectNode = buildNode(binaryOperation.left());
        LiteralNode patternLiteral = (LiteralNode) binaryOperation.right();
        return new ExecutableRegexOp(
                subjectNode,
                Pattern.compile(LiteralMaterializer.unquoteStringLiteral(patternLiteral.value())),
                negate);
    }

    private ExecutableNode buildBinaryOperation(BinaryOperationNode binaryOperation) {
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

    private ExecutableNode buildTernaryOperation(TernaryOperationNode ternaryOperation) {
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

    private ExecutableNode buildUnaryOperation(UnaryOperationNode unaryOperation) {
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

    private ExecutableNode buildPostfixOperation(PostfixOperationNode postfixOperation) {
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

    private ExecutableNode buildConditional(ConditionalNode conditional) {
        List<ExecutableNode> conditions = new ArrayList<>();
        List<ExecutableNode> results = new ArrayList<>();
        ExecutableNode elseExpression = null;

        for (int index = 0; index < conditional.conditions().size(); index++) {
            ExecutableNode conditionNode = buildNode(conditional.conditions().get(index));
            if (ConstantNodeValues.isConstant(conditionNode)) {
                if (Boolean.TRUE.equals(ConstantNodeValues.value(conditionNode))) {
                    elseExpression = buildNode(conditional.results().get(index));
                    break;
                }
            } else {
                conditions.add(conditionNode);
                results.add(buildNode(conditional.results().get(index)));
            }
        }

        if (elseExpression == null) {
            elseExpression = buildNode(conditional.elseExpression());
        }
        if (conditions.isEmpty()) {
            return elseExpression;
        }
        if (conditions.size() == 1) {
            return new ExecutableSimpleConditional(conditions.getFirst(), results.getFirst(), elseExpression);
        }
        return new ExecutableConditional(conditions, results, elseExpression);
    }

    private ExecutableNode buildVectorLiteral(VectorLiteralNode vectorLiteral) {
        List<ExecutableNode> elements = vectorLiteral.elements().stream()
                .map(this::buildNode)
                .toList();
        if (elements.stream().allMatch(ConstantNodeValues::isConstant)) {
            List<Object> foldedValues = elements.stream().map(ConstantNodeValues::value).toList();
            return new ExecutableVectorLiteral(elements, foldedValues);
        }
        return new ExecutableVectorLiteral(elements);
    }

    private ExecutableNode buildPropertyChain(PropertyChainNode node) {
        PropertyChainRootPlanner.PropertyChainRootPlan rootPlan = PropertyChainRootPlanner.build(
                node,
                model,
                externalSymbolCatalog,
                foldContext);
        ExecutableNode root = rootPlan.root();
        ResolvedType currentType = rootPlan.rootType();

        if (NavigationStepClassifier.isLegacyAccessChain(node.chain())) {
            return buildLegacyPropertyChain(node, root, currentType);
        }

        List<ExecutablePropertyChain.ExecutableAccess> steps = new ArrayList<>(node.chain().size());

        for (PropertyChainNode.MemberAccess access : node.chain()) {
            boolean safe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;

            switch (access) {
                case PropertyChainNode.CollectionIndexStep collectionIndexStep -> {
                    ExecutableNode index = buildNode(collectionIndexStep.index());
                    steps.add(new ExecutablePropertyChain.ExecutableIndexAccess(index));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.MapKeyStep mapKeyStep -> {
                    steps.add(new ExecutablePropertyChain.ExecutableMapKeyAccess(mapKeyStep.key()));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.CollectionSliceStep collectionSliceStep -> {
                    ExecutableNode start = collectionSliceStep.start() != null
                            ? buildNode(collectionSliceStep.start())
                            : null;
                    ExecutableNode end = collectionSliceStep.end() != null
                            ? buildNode(collectionSliceStep.end())
                            : null;
                    steps.add(new ExecutablePropertyChain.ExecutableSliceAccess(start, end));
                    continue;
                }
                case PropertyChainNode.WildcardStep ignored -> {
                    steps.add(new ExecutablePropertyChain.ExecutableWildcard());
                    continue;
                }
                case PropertyChainNode.FilterPredicateStep filterPredicateStep -> {
                    ExecutableNode predicate = buildNode(filterPredicateStep.predicate());
                    steps.add(new ExecutablePropertyChain.ExecutableFilterPredicate(predicate));
                    continue;
                }
                case PropertyChainNode.DeepScanStep deepScanStep -> {
                    steps.add(new ExecutablePropertyChain.ExecutableDeepScan(deepScanStep.propertyName()));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.CollectionFunctionStep collectionFunctionStep -> {
                    CollectionFunctionStepPlan stepPlan = buildCollectionFunctionStep(node, collectionFunctionStep);
                    steps.add(stepPlan.access());
                    currentType = stepPlan.returnType();
                    continue;
                }
                case PropertyChainNode.MapProjectionStep mapProjectionStep -> {
                    steps.add(new ExecutablePropertyChain.ExecutableMapProjection(mapProjectionStep.kind()));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.VectorMapStep vectorMapStep -> {
                    ExecutableNode transform = buildNode(vectorMapStep.transform());
                    steps.add(new ExecutablePropertyChain.ExecutableVectorMap(transform));
                    currentType = VectorType.INSTANCE;
                    continue;
                }
                case PropertyChainNode.VectorAggregationStep vectorAggregationStep -> {
                    if (vectorAggregationStep.kind() == VectorAggregationKind.COUNT
                            && !steps.isEmpty()
                            && steps.getLast() instanceof ExecutablePropertyChain.ExecutableMapProjection) {
                        steps.removeLast();
                    }
                    ExecutableNode transform = vectorAggregationStep.transform() == null
                            ? null
                            : buildNode(vectorAggregationStep.transform());
                    steps.add(new ExecutablePropertyChain.ExecutableVectorAggregation(vectorAggregationStep.kind(), transform));
                    currentType = UnknownType.INSTANCE;
                    continue;
                }
                default -> { /* fall through to typed/reflective path below */ }
            }

            AccessStepPlan accessPlan = buildMemberAccessStep(access, safe, currentType);
            steps.add(accessPlan.access());
            currentType = accessPlan.nextType();
        }
        return PropertyChainPrefixFolder.fold(root, steps, runtimeServices, mathContext);
    }

    private ExecutableNode buildLegacyPropertyChain(
            PropertyChainNode node,
            ExecutableNode root,
            ResolvedType currentType) {
        List<ExecutablePropertyChain.ExecutableAccess> steps = new ArrayList<>(node.chain().size());

        for (PropertyChainNode.MemberAccess access : node.chain()) {
            boolean safe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;
            AccessStepPlan accessPlan = buildMemberAccessStep(access, safe, currentType);
            steps.add(accessPlan.access());
            currentType = accessPlan.nextType();
        }
        return PropertyChainPrefixFolder.fold(root, steps, runtimeServices, mathContext);
    }

    private CollectionFunctionStepPlan buildCollectionFunctionStep(
            PropertyChainNode node,
            PropertyChainNode.CollectionFunctionStep collectionFunctionStep) {
        List<ExecutableNode> args = collectionFunctionStep.arguments().stream()
                .map(this::buildNode)
                .toList();
        ResolvedFunctionBinding binding = model.findFunctionBinding(node.nodeId()).orElse(null);
        NavigationMode resultMode = NavigationMode.SCALAR;
        ResolvedType returnType = UnknownType.INSTANCE;
        if (binding != null) {
            returnType = binding.returnType();
            if (returnType instanceof com.runestone.expeval.types.CollectionType || returnType == VectorType.INSTANCE) {
                resultMode = NavigationMode.COLLECTION;
            } else if (returnType instanceof com.runestone.expeval.types.MapType) {
                resultMode = NavigationMode.MAP;
            }
            return new CollectionFunctionStepPlan(
                    new ExecutablePropertyChain.ExecutableCollectionFunction(binding, args, resultMode),
                    returnType);
        }
        return new CollectionFunctionStepPlan(
                new ExecutablePropertyChain.ExecutableCollectionFunction(
                        new ResolvedFunctionBinding(null, null, UnknownType.INSTANCE), args, resultMode),
                returnType);
    }

    private AccessStepPlan buildMemberAccessStep(
            PropertyChainNode.MemberAccess access,
            boolean safe,
            ResolvedType currentType) {
        List<ExecutableNode> argumentNodes = buildAccessArguments(access);
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
                return new AccessStepPlan(typedStep.access(), typedStep.nextType());
            }
        }
        return new AccessStepPlan(
                ReflectiveAccessPlanner.build(access, argumentNodes, safe),
                UnknownType.INSTANCE);
    }

    private List<ExecutableNode> buildAccessArguments(PropertyChainNode.MemberAccess access) {
        return switch (access) {
            case PropertyChainNode.MethodCallAccess methodCall ->
                    methodCall.arguments().stream().map(this::buildNode).toList();
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                    safeMethodCall.arguments().stream().map(this::buildNode).toList();
            default -> List.of();
        };
    }

    private ExecutableNode buildIdentifier(IdentifierNode identifier) {
        if (LanguageSymbols.CURRENT_ELEMENT.equals(identifier.name())) {
            return new ExecutableIdentifier(
                    new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL),
                    identifier.sourceSpan());
        }
        SymbolRef ref = model.findSymbol(identifier.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing symbol for identifier '" + identifier.name() + "'"));
        if (foldContext.symbols.containsKey(ref)) {
            Object value = foldContext.symbols.get(ref);
            foldContext.variableReads.add(new AuditEvent.VariableRead(ref.name(), false, value));
            return new ExecutableLiteral(value);
        }
        return new ExecutableIdentifier(ref, identifier.sourceSpan());
    }

    private ExecutableNode buildFunctionCall(FunctionCallNode functionCall) {
        ResolvedFunctionBinding binding = model.findFunctionBinding(functionCall.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing function binding for '" + functionCall.functionName() + "'"));
        List<ExecutableNode> arguments = functionCall.arguments().stream()
                .map(this::buildNode)
                .toList();

        return FunctionCallPlanner.build(binding, arguments, runtimeServices);
    }

    private record AccessStepPlan(
            ExecutablePropertyChain.ExecutableAccess access,
            ResolvedType nextType
    ) {
    }

    private record CollectionFunctionStepPlan(
            ExecutablePropertyChain.ExecutableAccess access,
            ResolvedType returnType
    ) {
    }
}
