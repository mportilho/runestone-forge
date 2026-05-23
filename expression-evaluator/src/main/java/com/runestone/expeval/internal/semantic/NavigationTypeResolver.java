package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.catalog.FunctionCatalog;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.catalog.MethodDescriptor;
import com.runestone.expeval.catalog.PropertyDescriptor;
import com.runestone.expeval.catalog.TypeHintCatalog;
import com.runestone.expeval.catalog.TypeMetadata;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.types.CollectionType;
import com.runestone.expeval.types.MapType;
import com.runestone.expeval.types.NullType;
import com.runestone.expeval.types.ObjectType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ResolvedTypes;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;
import com.runestone.expeval.types.VectorType;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

final class NavigationTypeResolver {

    private final SemanticSymbolResolver symbolResolver;
    private final TypeHintCatalog typeHintCatalog;
    private final FunctionCatalog functionCatalog;
    private final Map<NodeId, ResolvedFunctionBinding> functionBindings;
    private final Function<ExpressionNode, ResolvedType> expressionResolver;
    private final SemanticErrorReporter errorReporter;

    /** Type of the @ element in the innermost active filter predicate. */
    @Nullable private ResolvedType filterElementType;

    NavigationTypeResolver(
            SemanticSymbolResolver symbolResolver,
            TypeHintCatalog typeHintCatalog,
            FunctionCatalog functionCatalog,
            Map<NodeId, ResolvedFunctionBinding> functionBindings,
            Function<ExpressionNode, ResolvedType> expressionResolver,
            SemanticErrorReporter errorReporter) {
        this.symbolResolver = Objects.requireNonNull(symbolResolver, "symbolResolver");
        this.typeHintCatalog = Objects.requireNonNull(typeHintCatalog, "typeHintCatalog");
        this.functionCatalog = Objects.requireNonNull(functionCatalog, "functionCatalog");
        this.functionBindings = Objects.requireNonNull(functionBindings, "functionBindings");
        this.expressionResolver = Objects.requireNonNull(expressionResolver, "expressionResolver");
        this.errorReporter = Objects.requireNonNull(errorReporter, "errorReporter");
    }

    @Nullable ResolvedType currentFilterElementType() {
        return filterElementType;
    }

    ResolvedType resolve(PropertyChainNode node) {
        if (LanguageSymbols.CURRENT_ELEMENT.equals(node.rootIdentifier())) {
            if (filterElementType == null) {
                error(IssueCode.INVALID_CURRENT_ELEMENT,
                        "'@' may only be used inside a filter predicate [?(...)]", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            if (NavigationStepClassifier.isLegacyAccessChain(node.chain())) {
                return resolveLegacyPropertyChain(filterElementType, node.chain(), node);
            }
            return resolveChainFrom(filterElementType, node.chain(), node);
        }

        ResolvedType current = resolveRootType(node);
        if (NavigationStepClassifier.isLegacyAccessChain(node.chain())) {
            return resolveLegacyPropertyChain(current, node.chain(), node);
        }
        return resolveChainFrom(current, node.chain(), node);
    }

    private ResolvedType resolveLegacyPropertyChain(
            ResolvedType start,
            List<PropertyChainNode.MemberAccess> chain,
            PropertyChainNode node) {
        ResolvedType current = start;

        for (PropertyChainNode.MemberAccess access : chain) {
            boolean isSafe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;
            List<ResolvedType> argumentTypes = methodArgumentTypes(access);

            if (current == UnknownType.INSTANCE || current == NullType.INSTANCE) {
                if (isSafe) {
                    current = NullType.INSTANCE;
                }
                continue;
            }
            if (!(current instanceof ObjectType objectType)) {
                if (isSafe) {
                    current = NullType.INSTANCE;
                    continue;
                }
                error(IssueCode.INVALID_MEMBER_ACCESS,
                        "type " + current + " does not support member access", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            TypeMetadata metadata = findMetadata(objectType);
            if (metadata == null) {
                current = UnknownType.INSTANCE;
                continue;
            }
            current = resolveLegacyObjectMember(metadata, access, argumentTypes, node.sourceSpan());
        }

        return current;
    }

    private ResolvedType resolveChainFrom(
            ResolvedType start,
            List<PropertyChainNode.MemberAccess> chain,
            PropertyChainNode node) {
        ResolvedType current = start;

        for (PropertyChainNode.MemberAccess access : chain) {
            boolean isSafe = access instanceof PropertyChainNode.SafePropertyAccess
                    || access instanceof PropertyChainNode.SafeMethodCallAccess;

            if (current == UnknownType.INSTANCE || current == NullType.INSTANCE) {
                if (isSafe) {
                    current = NullType.INSTANCE;
                }
                resolveAccessArgumentTypes(access);
                continue;
            }

            if (current instanceof CollectionType collectionType) {
                current = resolveCollectionStep(access, collectionType, node);
                continue;
            }
            if (current == VectorType.INSTANCE) {
                current = resolveVectorStep(access, node);
                continue;
            }
            if (current instanceof MapType mapType) {
                current = resolveMapStep(access, mapType, node);
                continue;
            }

            if (access instanceof PropertyChainNode.DeepScanStep deepScan) {
                current = resolveDeepScanStep(current, deepScan, node);
                continue;
            }
            if (access instanceof PropertyChainNode.WildcardStep) {
                current = new CollectionType(UnknownType.INSTANCE);
                continue;
            }
            if (access instanceof PropertyChainNode.VectorAggregationStep) {
                error(IssueCode.TYPE_MISMATCH, "aggregation step requires a collection type", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            if (access instanceof PropertyChainNode.MapProjectionStep) {
                error(IssueCode.TYPE_MISMATCH, "map projection step requires a MapType", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            if (access instanceof PropertyChainNode.CollectionFunctionStep collectionFunctionStep) {
                current = resolveCollectionFunction(collectionFunctionStep, current, node);
                continue;
            }
            if (access instanceof PropertyChainNode.FilterPredicateStep filterPredicateStep) {
                resolveFilterPredicate(filterPredicateStep.predicate(), UnknownType.INSTANCE);
                continue;
            }
            if (access instanceof PropertyChainNode.CollectionIndexStep collectionIndexStep) {
                resolveExpression(collectionIndexStep.index());
                if (isSafe) {
                    current = NullType.INSTANCE;
                    continue;
                }
                error(IssueCode.INVALID_MEMBER_ACCESS, "index access requires a collection type", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            if (access instanceof PropertyChainNode.MapKeyStep) {
                if (isSafe) {
                    current = NullType.INSTANCE;
                    continue;
                }
                error(IssueCode.INVALID_MEMBER_ACCESS, "key access requires a MapType", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            if (!(current instanceof ObjectType objectType)) {
                if (isSafe) {
                    current = NullType.INSTANCE;
                    continue;
                }
                resolveAccessArgumentTypes(access);
                continue;
            }
            TypeMetadata metadata = findMetadata(objectType);
            if (metadata == null) {
                current = UnknownType.INSTANCE;
                resolveAccessArgumentTypes(access);
                continue;
            }
            current = resolveObjectMember(metadata, access, methodArgumentTypes(access), node.sourceSpan());
        }
        return current;
    }

    private ResolvedType resolveCollectionStep(
            PropertyChainNode.MemberAccess access,
            CollectionType collectionType,
            PropertyChainNode node) {
        return switch (access) {
            case PropertyChainNode.CollectionIndexStep collectionIndexStep -> {
                resolveExpression(collectionIndexStep.index());
                yield collectionType.elementType();
            }
            case PropertyChainNode.CollectionSliceStep collectionSliceStep -> {
                resolveSliceBounds(collectionSliceStep);
                yield collectionType;
            }
            case PropertyChainNode.WildcardStep ignored -> collectionType;
            case PropertyChainNode.FilterPredicateStep filterPredicateStep -> {
                resolveFilterPredicate(filterPredicateStep.predicate(), collectionType.elementType());
                yield collectionType;
            }
            case PropertyChainNode.VectorAggregationStep(var kind, var transform) -> {
                if (transform != null) {
                    resolveFilterPredicate(transform, UnknownType.INSTANCE);
                }
                yield ScalarType.NUMBER;
            }
            case PropertyChainNode.VectorMapStep vectorMapStep -> {
                resolveFilterPredicate(vectorMapStep.transform(), collectionType.elementType());
                yield new CollectionType(UnknownType.INSTANCE);
            }
            case PropertyChainNode.CollectionFunctionStep collectionFunctionStep ->
                    resolveCollectionFunction(collectionFunctionStep, collectionType, node);
            case PropertyChainNode.PropertyAccess propertyAccess ->
                    resolveCollectionPropertyAccess(collectionType, propertyAccess, node.sourceSpan());
            case PropertyChainNode.DeepScanStep deepScanStep ->
                    resolveCollectionDeepScan(collectionType, deepScanStep, node.sourceSpan());
            default -> {
                resolveAccessArgumentTypes(access);
                yield UnknownType.INSTANCE;
            }
        };
    }

    private ResolvedType resolveVectorStep(
            PropertyChainNode.MemberAccess access,
            PropertyChainNode node) {
        return switch (access) {
            case PropertyChainNode.CollectionIndexStep collectionIndexStep -> {
                resolveExpression(collectionIndexStep.index());
                yield UnknownType.INSTANCE;
            }
            case PropertyChainNode.CollectionSliceStep collectionSliceStep -> {
                resolveSliceBounds(collectionSliceStep);
                yield VectorType.INSTANCE;
            }
            case PropertyChainNode.WildcardStep ignored -> VectorType.INSTANCE;
            case PropertyChainNode.FilterPredicateStep filterPredicateStep -> {
                resolveFilterPredicate(filterPredicateStep.predicate(), UnknownType.INSTANCE);
                yield VectorType.INSTANCE;
            }
            case PropertyChainNode.VectorAggregationStep(var kind, var transform) -> {
                if (transform != null) {
                    resolveFilterPredicate(transform, UnknownType.INSTANCE);
                }
                yield ScalarType.NUMBER;
            }
            case PropertyChainNode.VectorMapStep vectorMapStep -> {
                resolveFilterPredicate(vectorMapStep.transform(), UnknownType.INSTANCE);
                yield VectorType.INSTANCE;
            }
            case PropertyChainNode.CollectionFunctionStep collectionFunctionStep ->
                    resolveCollectionFunction(collectionFunctionStep, VectorType.INSTANCE, node);
            default -> {
                resolveAccessArgumentTypes(access);
                yield UnknownType.INSTANCE;
            }
        };
    }

    private ResolvedType resolveMapStep(
            PropertyChainNode.MemberAccess access,
            MapType mapType,
            PropertyChainNode node) {
        return switch (access) {
            case PropertyChainNode.MapKeyStep ignored -> mapType.valueType();
            case PropertyChainNode.WildcardStep ignored -> new CollectionType(mapType.valueType());
            case PropertyChainNode.FilterPredicateStep filterPredicateStep -> {
                resolveMapFilterPredicate(filterPredicateStep.predicate());
                yield mapType;
            }
            case PropertyChainNode.MapProjectionStep mapProjectionStep -> switch (mapProjectionStep.kind()) {
                case KEYS -> new CollectionType(mapType.keyType());
                case VALUES -> new CollectionType(mapType.valueType());
            };
            case PropertyChainNode.VectorMapStep vectorMapStep -> {
                resolveFilterPredicate(vectorMapStep.transform(), UnknownType.INSTANCE);
                yield new CollectionType(UnknownType.INSTANCE);
            }
            case PropertyChainNode.CollectionFunctionStep collectionFunctionStep ->
                    resolveCollectionFunction(collectionFunctionStep, mapType, node);
            case PropertyChainNode.PropertyAccess propertyAccess -> {
                error(IssueCode.INVALID_MAP_PROPERTY_ACCESS,
                        "map does not support dot-notation property access '" + propertyAccess.name()
                                + "'; use [\"key\"], [*], or a filter", node.sourceSpan());
                yield UnknownType.INSTANCE;
            }
            default -> {
                resolveAccessArgumentTypes(access);
                yield UnknownType.INSTANCE;
            }
        };
    }

    private ResolvedType resolveDeepScanStep(
            ResolvedType current,
            PropertyChainNode.DeepScanStep deepScanStep,
            PropertyChainNode node) {
        if (!(current instanceof ObjectType objectType) || deepScanStep.propertyName() == null) {
            return new CollectionType(UnknownType.INSTANCE);
        }
        TypeMetadata metadata = findMetadata(objectType);
        ResolvedType elementType = metadata != null
                ? resolveProperty(
                        metadata,
                        new PropertyChainNode.PropertyAccess(deepScanStep.propertyName()),
                        node.sourceSpan())
                : UnknownType.INSTANCE;
        return new CollectionType(elementType);
    }

    private ResolvedType resolveCollectionPropertyAccess(
            CollectionType collectionType,
            PropertyChainNode.PropertyAccess propertyAccess,
            SourceSpan sourceSpan) {
        if (collectionType.elementType() instanceof ObjectType objectType) {
            TypeMetadata metadata = findMetadata(objectType);
            if (metadata != null) {
                return new CollectionType(resolveProperty(metadata, propertyAccess, sourceSpan));
            }
        }
        return new CollectionType(UnknownType.INSTANCE);
    }

    private ResolvedType resolveCollectionDeepScan(
            CollectionType collectionType,
            PropertyChainNode.DeepScanStep deepScanStep,
            SourceSpan sourceSpan) {
        if (deepScanStep.propertyName() == null) {
            return new CollectionType(UnknownType.INSTANCE);
        }
        if (collectionType.elementType() instanceof ObjectType objectType) {
            TypeMetadata metadata = findMetadata(objectType);
            if (metadata != null) {
                return new CollectionType(resolveProperty(
                        metadata,
                        new PropertyChainNode.PropertyAccess(deepScanStep.propertyName()),
                        sourceSpan));
            }
        }
        return new CollectionType(UnknownType.INSTANCE);
    }

    private ResolvedType resolveCollectionFunction(
            PropertyChainNode.CollectionFunctionStep step,
            ResolvedType targetType,
            PropertyChainNode node) {
        List<ResolvedType> argumentTypes = step.arguments().stream()
                .map(this::resolveExpression)
                .toList();
        Collection<FunctionDescriptor> candidates = functionCatalog.findCandidates(step.name());
        if (candidates.isEmpty()) {
            error(IssueCode.UNKNOWN_COLLECTION_FUNCTION,
                    "no function '" + step.name() + "' found in catalog for collection/map call",
                    node.sourceSpan());
            return UnknownType.INSTANCE;
        }

        int expectedArity = step.arguments().size() + 1;
        FunctionDescriptor match = null;
        for (FunctionDescriptor candidate : candidates) {
            if (candidate.arity() != expectedArity || !matchesCollectionFunction(candidate, targetType, argumentTypes)) {
                continue;
            }
            if (match != null) {
                error(IssueCode.AMBIGUOUS_FUNCTION,
                        "ambiguous collection function call '" + step.name() + "'", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            match = candidate;
        }
        if (match == null) {
            error(IssueCode.INCOMPATIBLE_COLLECTION_FUNCTION_ARGUMENTS,
                    "no compatible overload for collection function '" + step.name() + "'",
                    node.sourceSpan());
            return UnknownType.INSTANCE;
        }

        ResolvedFunctionBinding binding = new ResolvedFunctionBinding(match.functionRef(), match, match.returnType());
        functionBindings.put(node.nodeId(), binding);
        return match.returnType();
    }

    private boolean matchesCollectionFunction(
            FunctionDescriptor candidate,
            ResolvedType targetType,
            List<ResolvedType> argumentTypes) {
        ResolvedType firstParameterType = candidate.parameterResolvedTypes().isEmpty()
                ? UnknownType.INSTANCE
                : candidate.parameterResolvedTypes().getFirst();
        if (firstParameterType != UnknownType.INSTANCE
                && firstParameterType != VectorType.INSTANCE
                && !(firstParameterType instanceof CollectionType)
                && targetType instanceof ObjectType) {
            return false;
        }
        List<ResolvedType> explicitParameterTypes = candidate.parameterResolvedTypes()
                .subList(1, candidate.parameterResolvedTypes().size());
        return SemanticTypeMatcher.matchesArguments(explicitParameterTypes, argumentTypes);
    }

    private ResolvedType resolveObjectMember(
            TypeMetadata metadata,
            PropertyChainNode.MemberAccess access,
            List<ResolvedType> argumentTypes,
            SourceSpan sourceSpan) {
        return switch (access) {
            case PropertyChainNode.PropertyAccess propertyAccess ->
                    resolveProperty(metadata, propertyAccess, sourceSpan);
            case PropertyChainNode.SafePropertyAccess safePropertyAccess ->
                    resolveProperty(metadata, new PropertyChainNode.PropertyAccess(safePropertyAccess.name()), sourceSpan);
            case PropertyChainNode.MethodCallAccess methodCall ->
                    resolveMethod(metadata, methodCall, argumentTypes, sourceSpan);
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                    resolveMethod(
                            metadata,
                            new PropertyChainNode.MethodCallAccess(safeMethodCall.name(), safeMethodCall.arguments()),
                            argumentTypes,
                            sourceSpan);
            default -> UnknownType.INSTANCE;
        };
    }

    private ResolvedType resolveLegacyObjectMember(
            TypeMetadata metadata,
            PropertyChainNode.MemberAccess access,
            List<ResolvedType> argumentTypes,
            SourceSpan sourceSpan) {
        return switch (access) {
            case PropertyChainNode.PropertyAccess propertyAccess ->
                    resolveProperty(metadata, propertyAccess, sourceSpan);
            case PropertyChainNode.SafePropertyAccess safePropertyAccess ->
                    resolveProperty(metadata, new PropertyChainNode.PropertyAccess(safePropertyAccess.name()), sourceSpan);
            case PropertyChainNode.MethodCallAccess methodCall ->
                    resolveMethod(metadata, methodCall, argumentTypes, sourceSpan);
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                    resolveMethod(
                            metadata,
                            new PropertyChainNode.MethodCallAccess(safeMethodCall.name(), safeMethodCall.arguments()),
                            argumentTypes,
                            sourceSpan);
            default -> throw new IllegalStateException("legacy property chain contains unsupported access: " + access);
        };
    }

    private List<ResolvedType> methodArgumentTypes(PropertyChainNode.MemberAccess access) {
        return switch (access) {
            case PropertyChainNode.MethodCallAccess methodCall ->
                    methodCall.arguments().stream().map(this::resolveExpression).toList();
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                    safeMethodCall.arguments().stream().map(this::resolveExpression).toList();
            default -> List.of();
        };
    }

    private void resolveAccessArgumentTypes(PropertyChainNode.MemberAccess access) {
        switch (access) {
            case PropertyChainNode.MethodCallAccess methodCall ->
                    methodCall.arguments().forEach(this::resolveExpression);
            case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                    safeMethodCall.arguments().forEach(this::resolveExpression);
            case PropertyChainNode.CollectionFunctionStep collectionFunctionStep ->
                    collectionFunctionStep.arguments().forEach(this::resolveExpression);
            case PropertyChainNode.CollectionIndexStep collectionIndexStep ->
                    resolveExpression(collectionIndexStep.index());
            case PropertyChainNode.CollectionSliceStep collectionSliceStep ->
                    resolveSliceBounds(collectionSliceStep);
            case PropertyChainNode.FilterPredicateStep filterPredicateStep ->
                    resolveFilterPredicate(filterPredicateStep.predicate(), UnknownType.INSTANCE);
            case PropertyChainNode.VectorMapStep vectorMapStep ->
                    resolveFilterPredicate(vectorMapStep.transform(), UnknownType.INSTANCE);
            default -> {
            }
        }
    }

    private void resolveSliceBounds(PropertyChainNode.CollectionSliceStep collectionSliceStep) {
        if (collectionSliceStep.start() != null) {
            resolveExpression(collectionSliceStep.start());
        }
        if (collectionSliceStep.end() != null) {
            resolveExpression(collectionSliceStep.end());
        }
    }

    private void resolveFilterPredicate(ExpressionNode predicate, ResolvedType elementType) {
        ResolvedType savedElementType = filterElementType;
        filterElementType = elementType;
        try {
            resolveExpression(predicate);
        } finally {
            filterElementType = savedElementType;
        }
    }

    private void resolveMapFilterPredicate(ExpressionNode predicate) {
        resolveFilterPredicate(predicate, UnknownType.INSTANCE);
    }

    private ResolvedType resolveRootType(PropertyChainNode node) {
        return symbolResolver.resolveRootType(node, filterElementType);
    }

    @Nullable private TypeMetadata findMetadata(ObjectType objectType) {
        return typeHintCatalog.find(objectType.javaClass()).orElse(null);
    }

    private ResolvedType resolveProperty(
            TypeMetadata metadata,
            PropertyChainNode.PropertyAccess access,
            SourceSpan sourceSpan) {
        PropertyDescriptor descriptor = metadata.properties().get(access.name());
        if (descriptor == null) {
            error(IssueCode.UNKNOWN_PROPERTY,
                    "property '" + access.name() + "' not found on " + metadata.javaClass().getSimpleName(),
                    sourceSpan);
            return UnknownType.INSTANCE;
        }
        return descriptor.resolvedType();
    }

    private ResolvedType resolveMethod(
            TypeMetadata metadata,
            PropertyChainNode.MethodCallAccess access,
            List<ResolvedType> argumentTypes,
            SourceSpan sourceSpan) {
        List<MethodDescriptor> candidates = metadata.methods().get(access.name());
        if (candidates == null || candidates.isEmpty()) {
            error(IssueCode.UNKNOWN_METHOD,
                    "method '" + access.name() + "' not found on " + metadata.javaClass().getSimpleName(),
                    sourceSpan);
            return UnknownType.INSTANCE;
        }

        int expectedArity = access.arguments().size();
        List<MethodDescriptor> arityMatches = candidates.stream()
                .filter(candidate -> candidate.arity() == expectedArity)
                .toList();
        if (arityMatches.isEmpty()) {
            error(IssueCode.INVALID_METHOD_ARITY,
                    "invalid arity for method '" + access.name() + "' on " + metadata.javaClass().getSimpleName(),
                    sourceSpan);
            return UnknownType.INSTANCE;
        }

        MethodDescriptor exactMatch = null;
        for (MethodDescriptor candidate : arityMatches) {
            if (!matchesMethodArguments(candidate, argumentTypes)) {
                continue;
            }
            if (exactMatch != null) {
                error(IssueCode.AMBIGUOUS_METHOD,
                        "ambiguous method call '" + access.name() + "' on " + metadata.javaClass().getSimpleName(),
                        sourceSpan);
                return UnknownType.INSTANCE;
            }
            exactMatch = candidate;
        }
        if (exactMatch == null) {
            error(IssueCode.INCOMPATIBLE_METHOD_ARGUMENTS,
                    "incompatible arguments for method '" + access.name() + "' on " + metadata.javaClass().getSimpleName(),
                    sourceSpan);
            return UnknownType.INSTANCE;
        }
        return exactMatch.returnType();
    }

    private boolean matchesMethodArguments(MethodDescriptor descriptor, List<ResolvedType> argumentTypes) {
        for (int index = 0; index < argumentTypes.size(); index++) {
            ResolvedType actualType = argumentTypes.get(index);
            ResolvedType expectedType = resolveJavaType(descriptor.parameterTypes().get(index));
            if (actualType != UnknownType.INSTANCE
                    && expectedType != UnknownType.INSTANCE
                    && !actualType.equals(expectedType)) {
                return false;
            }
        }
        return true;
    }

    private ResolvedType resolveJavaType(Class<?> javaType) {
        return typeHintCatalog.isRegistered(javaType)
                ? new ObjectType(javaType)
                : ResolvedTypes.fromJavaType(javaType);
    }

    private ResolvedType resolveExpression(ExpressionNode node) {
        return expressionResolver.apply(node);
    }

    private void error(IssueCode code, String message, SourceSpan sourceSpan) {
        errorReporter.error(code, message, sourceSpan);
    }
}
