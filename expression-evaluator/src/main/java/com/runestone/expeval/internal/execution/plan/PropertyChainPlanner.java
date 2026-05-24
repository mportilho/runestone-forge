package com.runestone.expeval.internal.execution.plan;

import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.TypeHintCatalog;
import com.runestone.expeval.catalog.TypeMetadata;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.internal.navigation.NavigationMode;
import com.runestone.expeval.internal.navigation.VectorAggregationKind;
import com.runestone.expeval.internal.runtime.RuntimeServices;
import com.runestone.expeval.internal.semantic.NavigationStepClassifier;
import com.runestone.expeval.internal.semantic.ResolvedFunctionBinding;
import com.runestone.expeval.internal.semantic.SemanticModel;
import com.runestone.expeval.types.CollectionType;
import com.runestone.expeval.types.MapType;
import com.runestone.expeval.types.ObjectType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.UnknownType;
import com.runestone.expeval.types.VectorType;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

final class PropertyChainPlanner {

    private final SemanticModel model;
    private final RuntimeServices runtimeServices;
    private final ExternalSymbolCatalog externalSymbolCatalog;
    private final TypeHintCatalog typeHintCatalog;
    private final MathContext mathContext;
    private final ConstantFoldContext foldContext;
    private final Function<ExpressionNode, ExecutableNode> nodeBuilder;

    PropertyChainPlanner(
            SemanticModel model,
            RuntimeServices runtimeServices,
            ExternalSymbolCatalog externalSymbolCatalog,
            TypeHintCatalog typeHintCatalog,
            MathContext mathContext,
            ConstantFoldContext foldContext,
            Function<ExpressionNode, ExecutableNode> nodeBuilder) {
        this.model = Objects.requireNonNull(model, "model");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices");
        this.externalSymbolCatalog = Objects.requireNonNull(externalSymbolCatalog, "externalSymbolCatalog");
        this.typeHintCatalog = Objects.requireNonNull(typeHintCatalog, "typeHintCatalog");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext");
        this.foldContext = Objects.requireNonNull(foldContext, "foldContext");
        this.nodeBuilder = Objects.requireNonNull(nodeBuilder, "nodeBuilder");
    }

    ExecutableNode build(PropertyChainNode node) {
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
            resultMode = resultMode(returnType);
            return new CollectionFunctionStepPlan(
                    new ExecutablePropertyChain.ExecutableCollectionFunction(binding, args, resultMode),
                    returnType);
        }
        return new CollectionFunctionStepPlan(
                new ExecutablePropertyChain.ExecutableCollectionFunction(
                        new ResolvedFunctionBinding(null, null, UnknownType.INSTANCE), args, resultMode),
                returnType);
    }

    private static NavigationMode resultMode(ResolvedType returnType) {
        if (returnType instanceof CollectionType || returnType == VectorType.INSTANCE) {
            return NavigationMode.COLLECTION;
        }
        if (returnType instanceof MapType) {
            return NavigationMode.MAP;
        }
        return NavigationMode.SCALAR;
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

    private ExecutableNode buildNode(ExpressionNode node) {
        return nodeBuilder.apply(node);
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
