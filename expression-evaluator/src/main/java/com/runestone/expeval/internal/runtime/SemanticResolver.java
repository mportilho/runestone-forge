package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.ast.AssignmentNode;
import com.runestone.expeval.internal.ast.BinaryOperationNode;
import com.runestone.expeval.internal.ast.ConditionalNode;
import com.runestone.expeval.internal.ast.DestructuringAssignmentNode;
import com.runestone.expeval.internal.ast.ExpressionFileNode;
import com.runestone.expeval.internal.ast.ExpressionNode;
import com.runestone.expeval.internal.ast.FunctionCallNode;
import com.runestone.expeval.internal.ast.IdentifierNode;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.PostfixOperationNode;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.internal.ast.SimpleAssignmentNode;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.ast.TernaryOperationNode;
import com.runestone.expeval.internal.ast.UnaryOperationNode;
import com.runestone.expeval.internal.ast.VectorLiteralNode;
import com.runestone.expeval.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval.catalog.FunctionDescriptor;
import com.runestone.expeval.catalog.MethodDescriptor;
import com.runestone.expeval.catalog.PropertyDescriptor;
import com.runestone.expeval.catalog.TypeMetadata;
import com.runestone.expeval.types.CollectionType;
import com.runestone.expeval.types.MapType;
import com.runestone.expeval.types.ObjectType;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ResolvedTypes;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.NullType;
import com.runestone.expeval.types.UnknownType;
import com.runestone.expeval.types.VectorType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class SemanticResolver {

    public SemanticModel resolve(ExpressionFileNode file, ResolutionContext context) {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(context, "context must not be null");
        ResolutionSession session = new ResolutionSession(context);
        session.collectInternalSymbols(file.assignments());
        for (AssignmentNode assignment : file.assignments()) {
            session.resolveAssignment(assignment);
        }
        if (file.resultExpression() != null) {
            ResolvedType resultType = session.resolveExpression(file.resultExpression());
            boolean tolerateUnknownPropertyChain = file.resultExpression() instanceof PropertyChainNode
                    && resultType == UnknownType.INSTANCE;
            if (!tolerateUnknownPropertyChain
                    && context.resultType() == ExpressionResultType.MATH
                    && resultType != UnknownType.INSTANCE
                    && resultType != ScalarType.NUMBER) {
                session.error("RESULT_TYPE_MISMATCH", "math expressions must resolve to NUMBER", file.resultExpression().sourceSpan());
            }
            if (!tolerateUnknownPropertyChain
                    && context.resultType() == ExpressionResultType.LOGICAL
                    && resultType != UnknownType.INSTANCE
                    && resultType != ScalarType.BOOLEAN) {
                session.error("RESULT_TYPE_MISMATCH", "logical expressions must resolve to BOOLEAN", file.resultExpression().sourceSpan());
            }
            session.resolvedTypes.put(file.nodeId(), resultType);
        } else {
            session.resolvedTypes.put(file.nodeId(), UnknownType.INSTANCE);
        }
        return new SemanticModel(
            file,
            session.resolvedTypes,
            session.symbolByNodeId,
            session.identifierUsages,
            session.assignmentUsages,
            session.externalSymbolsByName,
            session.internalSymbolsByName,
            session.functionBindings,
            session.issues
        );
    }

    private static final class ResolutionSession {

        private final ResolutionContext context;
        private final Map<NodeId, ResolvedType> resolvedTypes = new LinkedHashMap<>();
        private final Map<NodeId, SymbolRef> symbolByNodeId = new LinkedHashMap<>();
        private final Map<SymbolRef, List<IdentifierNode>> identifierUsages = new LinkedHashMap<>();
        private final Map<SymbolRef, List<AssignmentNode>> assignmentUsages = new LinkedHashMap<>();
        private final Map<String, SymbolRef> externalSymbolsByName = new LinkedHashMap<>();
        private final Map<String, SymbolRef> internalSymbolsByName = new LinkedHashMap<>();
        private final Map<NodeId, ResolvedFunctionBinding> functionBindings = new LinkedHashMap<>();
        private final Map<SymbolRef, ResolvedType> internalTypes = new LinkedHashMap<>();
        private final List<SemanticIssue> issues = new ArrayList<>();

        private ResolutionSession(ResolutionContext context) {
            this.context = context;
        }

        private void collectInternalSymbols(List<AssignmentNode> assignments) {
            for (AssignmentNode assignment : assignments) {
                switch (assignment) {
                    case SimpleAssignmentNode simpleAssignment -> registerInternal(simpleAssignment.targetName());
                    case DestructuringAssignmentNode destructuringAssignment ->
                        destructuringAssignment.targetNames().forEach(this::registerInternal);
                }
            }
        }

        private void resolveAssignment(AssignmentNode assignment) {
            switch (assignment) {
                case SimpleAssignmentNode simpleAssignment -> {
                    SymbolRef symbolRef = internalSymbolsByName.get(simpleAssignment.targetName());
                    ResolvedType valueType = resolveExpression(simpleAssignment.value());
                    resolvedTypes.put(simpleAssignment.nodeId(), valueType);
                    symbolByNodeId.put(simpleAssignment.nodeId(), symbolRef);
                    assignmentUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(simpleAssignment);
                    internalTypes.put(symbolRef, valueType);
                }
                case DestructuringAssignmentNode destructuringAssignment -> {
                    ResolvedType valueType = resolveExpression(destructuringAssignment.value());
                    resolvedTypes.put(destructuringAssignment.nodeId(), valueType == UnknownType.INSTANCE ? UnknownType.INSTANCE : VectorType.INSTANCE);
                    for (String targetName : destructuringAssignment.targetNames()) {
                        SymbolRef symbolRef = internalSymbolsByName.get(targetName);
                        assignmentUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(destructuringAssignment);
                        internalTypes.putIfAbsent(symbolRef, UnknownType.INSTANCE);
                    }
                }
            }
        }

        private ResolvedType resolveExpression(ExpressionNode node) {
            ResolvedType cached = resolvedTypes.get(node.nodeId());
            if (cached != null) {
                return cached;
            }
            ResolvedType resolvedType = switch (node) {
                case LiteralNode literalNode -> inferLiteralType(literalNode);
                case IdentifierNode identifierNode -> resolveIdentifier(identifierNode);
                case PropertyChainNode propertyChainNode -> resolvePropertyChain(propertyChainNode);
                case FunctionCallNode functionCallNode -> resolveFunctionCall(functionCallNode);
                case ConditionalNode conditionalNode -> resolveConditional(conditionalNode);
                case UnaryOperationNode unaryOperationNode -> resolveUnary(unaryOperationNode);
                case BinaryOperationNode binaryOperationNode -> resolveBinary(binaryOperationNode);
                case TernaryOperationNode ternaryOperationNode -> resolveTernary(ternaryOperationNode);
                case PostfixOperationNode postfixOperationNode -> resolvePostfix(postfixOperationNode);
                case VectorLiteralNode vectorLiteralNode -> resolveVector(vectorLiteralNode);
            };
            resolvedTypes.put(node.nodeId(), resolvedType);
            return resolvedType;
        }

        private ResolvedType inferLiteralType(LiteralNode node) {
            String value = node.value();
            if ("null".equals(value)) {
                return NullType.INSTANCE;
            }
            if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                return ScalarType.BOOLEAN;
            }
            if ("currDate".equals(value) || canParseDate(value)) {
                return ScalarType.DATE;
            }
            if ("currTime".equals(value) || canParseTime(value)) {
                return ScalarType.TIME;
            }
            if ("currDateTime".equals(value) || canParseDateTime(value)) {
                return ScalarType.DATETIME;
            }
            if (value.startsWith("\"") && value.endsWith("\"")) {
                return ScalarType.STRING;
            }
            try {
                new BigDecimal(value);
                return ScalarType.NUMBER;
            } catch (NumberFormatException ignored) {
                return UnknownType.INSTANCE;
            }
        }

        private ResolvedType resolveIdentifier(IdentifierNode node) {
            // '@' sentinel — current element in an active filter predicate
            if (CURRENT_ELEMENT.equals(node.name())) {
                if (filterElementType == null) {
                    error("INVALID_CURRENT_ELEMENT",
                            "'@' may only be used inside a filter predicate [?(...)]", node.sourceSpan());
                    return UnknownType.INSTANCE;
                }
                return filterElementType;
            }

            SymbolRef symbolRef = internalSymbolsByName.get(node.name());
            if (symbolRef != null) {
                symbolByNodeId.put(node.nodeId(), symbolRef);
                identifierUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(node);
                return internalTypes.getOrDefault(symbolRef, UnknownType.INSTANCE);
            }

            symbolRef = externalSymbolsByName.computeIfAbsent(node.name(), name -> new SymbolRef(name, SymbolKind.EXTERNAL));
            symbolByNodeId.put(node.nodeId(), symbolRef);
            identifierUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(node);
            return context.externalSymbolCatalog().find(node.name())
                .map(ExternalSymbolDescriptor::declaredType)
                .orElse(UnknownType.INSTANCE);
        }

        /** Current-element sentinel used in filter predicates ({@code @}). */
        private static final String CURRENT_ELEMENT = "@";

        /**
         * Type of the {@code @} element in the innermost active filter predicate.
         * {@code null} when no filter is active.
         */
        private ResolvedType filterElementType = null;

        private ResolvedType resolvePropertyChain(PropertyChainNode node) {
            // Handle '@' root — refers to current element inside a filter predicate
            if (CURRENT_ELEMENT.equals(node.rootIdentifier())) {
                if (filterElementType == null) {
                    error("INVALID_CURRENT_ELEMENT",
                            "'@' may only be used inside a filter predicate [?(...)]", node.sourceSpan());
                    return UnknownType.INSTANCE;
                }
                if (isLegacyAccessChain(node.chain())) {
                    return resolveLegacyPropertyChain(filterElementType, node.chain(), node);
                }
                return resolveChainFrom(filterElementType, node.chain(), node, true);
            }
            ResolvedType current = resolveRootType(node);
            if (isLegacyAccessChain(node.chain())) {
                return resolveLegacyPropertyChain(current, node.chain(), node);
            }
            return resolveChainFrom(current, node.chain(), node, false);
        }

        private ResolvedType resolveLegacyPropertyChain(
                ResolvedType start,
                List<PropertyChainNode.MemberAccess> chain,
                PropertyChainNode node) {
            ResolvedType current = start;

            for (PropertyChainNode.MemberAccess access : chain) {
                boolean isSafe = access instanceof PropertyChainNode.SafePropertyAccess
                        || access instanceof PropertyChainNode.SafeMethodCallAccess;
                List<ResolvedType> argumentTypes = switch (access) {
                    case PropertyChainNode.MethodCallAccess methodCall ->
                            methodCall.arguments().stream().map(this::resolveExpression).toList();
                    case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                            safeMethodCall.arguments().stream().map(this::resolveExpression).toList();
                    default -> List.of();
                };

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
                    error("INVALID_MEMBER_ACCESS",
                            "type " + current + " does not support member access", node.sourceSpan());
                    return UnknownType.INSTANCE;
                }
                TypeMetadata metadata = context.typeHintCatalog()
                        .find(objectType.javaClass()).orElse(null);
                if (metadata == null) {
                    current = UnknownType.INSTANCE;
                    continue;
                }
                current = switch (access) {
                    case PropertyChainNode.PropertyAccess propertyAccess ->
                            resolveProperty(metadata, propertyAccess, node.sourceSpan());
                    case PropertyChainNode.SafePropertyAccess safePropertyAccess ->
                            resolveProperty(metadata,
                                    new PropertyChainNode.PropertyAccess(safePropertyAccess.name()),
                                    node.sourceSpan());
                    case PropertyChainNode.MethodCallAccess methodCall ->
                            resolveMethod(metadata, methodCall, argumentTypes, node.sourceSpan());
                    case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                            resolveMethod(metadata,
                                    new PropertyChainNode.MethodCallAccess(
                                            safeMethodCall.name(), safeMethodCall.arguments()),
                                    argumentTypes,
                                    node.sourceSpan());
                    default -> throw new IllegalStateException("legacy property chain contains unsupported access: " + access);
                };
            }

            return current;
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

        private ResolvedType resolveChainFrom(
                ResolvedType start,
                List<PropertyChainNode.MemberAccess> chain,
                PropertyChainNode node,
                boolean insideFilter) {
            ResolvedType current = start;
            boolean vectorMode = current instanceof CollectionType || current == VectorType.INSTANCE;

            for (PropertyChainNode.MemberAccess access : chain) {
                boolean isSafe = access instanceof PropertyChainNode.SafePropertyAccess
                        || access instanceof PropertyChainNode.SafeMethodCallAccess;

                if (current == UnknownType.INSTANCE || current == NullType.INSTANCE) {
                    if (isSafe) current = NullType.INSTANCE;
                    // Resolve argument types even in unknown state (for side effects in functionBindings)
                    resolveAccessArgumentTypes(access);
                    continue;
                }

                // ----- Collection/Vector steps -----
                if (current instanceof CollectionType collectionType) {
                    current = resolveCollectionStep(access, collectionType, node);
                    vectorMode = current instanceof CollectionType || current == VectorType.INSTANCE;
                    continue;
                }
                if (current == VectorType.INSTANCE) {
                    current = resolveVectorStep(access, node);
                    vectorMode = current instanceof CollectionType || current == VectorType.INSTANCE;
                    continue;
                }

                // ----- Map steps -----
                if (current instanceof MapType mapType) {
                    current = resolveMapStep(access, mapType, node);
                    vectorMode = current instanceof CollectionType || current == VectorType.INSTANCE;
                    continue;
                }

                // ----- Scalar / ObjectType steps -----

                // Deep scan and wildcard steps can start from ObjectType
                if (access instanceof PropertyChainNode.DeepScanStep deepScan) {
                    if (current instanceof ObjectType objectType) {
                        // Deep scan of a named property returns a collection of that property's type
                        if (deepScan.propertyName() == null) {
                            // ..*: unknown element type
                            current = new CollectionType(UnknownType.INSTANCE);
                        } else {
                            TypeMetadata meta = context.typeHintCatalog().find(objectType.javaClass()).orElse(null);
                            ResolvedType elemType = meta != null
                                    ? resolveProperty(meta, new PropertyChainNode.PropertyAccess(deepScan.propertyName()), node.sourceSpan())
                                    : UnknownType.INSTANCE;
                            current = new CollectionType(elemType);
                        }
                    } else {
                        current = new CollectionType(UnknownType.INSTANCE);
                    }
                    vectorMode = true;
                    continue;
                }

                if (access instanceof PropertyChainNode.WildcardStep) {
                    current = new CollectionType(UnknownType.INSTANCE);
                    vectorMode = true;
                    continue;
                }

                if (access instanceof PropertyChainNode.VectorAggregationStep) {
                    error("TYPE_MISMATCH", "aggregation step requires a collection type", node.sourceSpan());
                    return UnknownType.INSTANCE;
                }

                if (access instanceof PropertyChainNode.MapProjectionStep) {
                    error("TYPE_MISMATCH", "map projection step requires a MapType", node.sourceSpan());
                    return UnknownType.INSTANCE;
                }

                if (access instanceof PropertyChainNode.CollectionFunctionStep cfs) {
                    // Try to find a compatible function in the catalog
                    current = resolveCollectionFunction(cfs, current, node);
                    vectorMode = current instanceof CollectionType || current == VectorType.INSTANCE;
                    continue;
                }

                if (access instanceof PropertyChainNode.FilterPredicateStep fps) {
                    resolveFilterPredicate(fps.predicate(), UnknownType.INSTANCE, node);
                    continue;
                }

                if (access instanceof PropertyChainNode.CollectionIndexStep cis) {
                    resolveExpression(cis.index());
                    if (isSafe) {
                        current = NullType.INSTANCE;
                        continue;
                    }
                    error("INVALID_MEMBER_ACCESS", "index access requires a collection type", node.sourceSpan());
                    return UnknownType.INSTANCE;
                }

                if (access instanceof PropertyChainNode.MapKeyStep) {
                    if (isSafe) {
                        current = NullType.INSTANCE;
                        continue;
                    }
                    error("INVALID_MEMBER_ACCESS", "key access requires a MapType", node.sourceSpan());
                    return UnknownType.INSTANCE;
                }

                // ----- Existing ObjectType property/method access -----
                if (!(current instanceof ObjectType objectType)) {
                    if (isSafe) {
                        current = NullType.INSTANCE;
                        continue;
                    }
                    // In vector mode, pass-through property access maps element-to-element
                    if (vectorMode) {
                        resolveAccessArgumentTypes(access);
                        continue;
                    }
                    resolveAccessArgumentTypes(access);
                    continue;
                }
                TypeMetadata metadata = context.typeHintCatalog()
                        .find(objectType.javaClass()).orElse(null);
                if (metadata == null) {
                    current = UnknownType.INSTANCE;
                    resolveAccessArgumentTypes(access);
                    continue;
                }
                List<ResolvedType> argumentTypes = switch (access) {
                    case PropertyChainNode.MethodCallAccess mc ->
                            mc.arguments().stream().map(this::resolveExpression).toList();
                    case PropertyChainNode.SafeMethodCallAccess smc ->
                            smc.arguments().stream().map(this::resolveExpression).toList();
                    default -> List.of();
                };
                current = switch (access) {
                    case PropertyChainNode.PropertyAccess propertyAccess ->
                            resolveProperty(metadata, propertyAccess, node.sourceSpan());
                    case PropertyChainNode.SafePropertyAccess safePropertyAccess ->
                            resolveProperty(metadata,
                                    new PropertyChainNode.PropertyAccess(safePropertyAccess.name()),
                                    node.sourceSpan());
                    case PropertyChainNode.MethodCallAccess methodCall ->
                            resolveMethod(metadata, methodCall, argumentTypes, node.sourceSpan());
                    case PropertyChainNode.SafeMethodCallAccess safeMethodCall ->
                            resolveMethod(metadata,
                                    new PropertyChainNode.MethodCallAccess(
                                            safeMethodCall.name(), safeMethodCall.arguments()),
                                    argumentTypes, node.sourceSpan());
                    default -> UnknownType.INSTANCE;
                };
                vectorMode = current instanceof CollectionType || current == VectorType.INSTANCE;
            }
            return current;
        }

        private void resolveAccessArgumentTypes(PropertyChainNode.MemberAccess access) {
            switch (access) {
                case PropertyChainNode.MethodCallAccess mc -> mc.arguments().forEach(this::resolveExpression);
                case PropertyChainNode.SafeMethodCallAccess smc -> smc.arguments().forEach(this::resolveExpression);
                case PropertyChainNode.CollectionFunctionStep cfs -> cfs.arguments().forEach(this::resolveExpression);
                case PropertyChainNode.CollectionIndexStep cis -> resolveExpression(cis.index());
                case PropertyChainNode.CollectionSliceStep css -> {
                    if (css.start() != null) resolveExpression(css.start());
                    if (css.end() != null) resolveExpression(css.end());
                }
                case PropertyChainNode.FilterPredicateStep fps ->
                        resolveFilterPredicate(fps.predicate(), UnknownType.INSTANCE, null);
                default -> { /* nothing */ }
            }
        }

        private ResolvedType resolveCollectionStep(
                PropertyChainNode.MemberAccess access,
                CollectionType collectionType,
                PropertyChainNode node) {
            return switch (access) {
                case PropertyChainNode.CollectionIndexStep cis -> {
                    resolveExpression(cis.index());
                    yield collectionType.elementType();
                }
                case PropertyChainNode.CollectionSliceStep css -> {
                    if (css.start() != null) resolveExpression(css.start());
                    if (css.end() != null) resolveExpression(css.end());
                    yield collectionType;
                }
                case PropertyChainNode.WildcardStep ignored -> collectionType;
                case PropertyChainNode.FilterPredicateStep fps -> {
                    resolveFilterPredicate(fps.predicate(), collectionType.elementType(), node);
                    yield collectionType;
                }
                case PropertyChainNode.VectorAggregationStep vas -> ScalarType.NUMBER;
                case PropertyChainNode.CollectionFunctionStep cfs ->
                        resolveCollectionFunction(cfs, collectionType, node);
                case PropertyChainNode.PropertyAccess pa -> {
                    // In collection mode, property access maps element to element
                    if (collectionType.elementType() instanceof ObjectType ot) {
                        TypeMetadata meta = context.typeHintCatalog().find(ot.javaClass()).orElse(null);
                        if (meta != null) {
                            yield new CollectionType(resolveProperty(meta, pa, node.sourceSpan()));
                        }
                    }
                    yield new CollectionType(UnknownType.INSTANCE);
                }
                case PropertyChainNode.DeepScanStep ds -> {
                    if (ds.propertyName() == null) {
                        yield new CollectionType(UnknownType.INSTANCE);
                    }
                    if (collectionType.elementType() instanceof ObjectType ot) {
                        TypeMetadata meta = context.typeHintCatalog().find(ot.javaClass()).orElse(null);
                        if (meta != null) {
                            yield new CollectionType(resolveProperty(meta,
                                    new PropertyChainNode.PropertyAccess(ds.propertyName()), node.sourceSpan()));
                        }
                    }
                    yield new CollectionType(UnknownType.INSTANCE);
                }
                default -> {
                    resolveAccessArgumentTypes(access);
                    yield UnknownType.INSTANCE;
                }
            };
        }

        private ResolvedType resolveVectorStep(
                PropertyChainNode.MemberAccess access,
                PropertyChainNode node) {
            switch (access) {
                case PropertyChainNode.CollectionIndexStep cis -> { resolveExpression(cis.index()); return UnknownType.INSTANCE; }
                case PropertyChainNode.CollectionSliceStep css -> {
                    if (css.start() != null) resolveExpression(css.start());
                    if (css.end() != null) resolveExpression(css.end());
                    return VectorType.INSTANCE;
                }
                case PropertyChainNode.WildcardStep ignored -> { return VectorType.INSTANCE; }
                case PropertyChainNode.FilterPredicateStep fps -> {
                    resolveFilterPredicate(fps.predicate(), UnknownType.INSTANCE, node);
                    return VectorType.INSTANCE;
                }
                case PropertyChainNode.VectorAggregationStep ignored -> { return ScalarType.NUMBER; }
                case PropertyChainNode.CollectionFunctionStep cfs ->
                        { return resolveCollectionFunction(cfs, VectorType.INSTANCE, node); }
                default -> {
                    resolveAccessArgumentTypes(access);
                    return UnknownType.INSTANCE;
                }
            }
        }

        private ResolvedType resolveMapStep(
                PropertyChainNode.MemberAccess access,
                MapType mapType,
                PropertyChainNode node) {
            return switch (access) {
                case PropertyChainNode.MapKeyStep ignored -> mapType.valueType();
                case PropertyChainNode.WildcardStep ignored -> new CollectionType(mapType.valueType());
                case PropertyChainNode.FilterPredicateStep fps -> {
                    resolveMapFilterPredicate(fps.predicate(), mapType, node);
                    yield mapType;
                }
                case PropertyChainNode.MapProjectionStep mps -> switch (mps.kind()) {
                    case KEYS -> new CollectionType(mapType.keyType());
                    case VALUES -> new CollectionType(mapType.valueType());
                };
                case PropertyChainNode.CollectionFunctionStep cfs ->
                        resolveCollectionFunction(cfs, mapType, node);
                case PropertyChainNode.PropertyAccess pa -> {
                    error("INVALID_MAP_PROPERTY_ACCESS",
                            "map does not support dot-notation property access '" + pa.name()
                                    + "'; use [\"key\"], [*], or a filter", node.sourceSpan());
                    yield UnknownType.INSTANCE;
                }
                default -> {
                    resolveAccessArgumentTypes(access);
                    yield UnknownType.INSTANCE;
                }
            };
        }

        private void resolveFilterPredicate(
                ExpressionNode predicate, ResolvedType elementType, @Nullable PropertyChainNode node) {
            ResolvedType savedElementType = filterElementType;
            filterElementType = elementType;
            try {
                resolveExpression(predicate);
            } finally {
                filterElementType = savedElementType;
            }
        }

        private void resolveMapFilterPredicate(
                ExpressionNode predicate, MapType mapType, PropertyChainNode node) {
            // During map filter resolution, @.key and @.value are the two valid access patterns.
            // We simply resolve with UnknownType for @ (the evaluator handles it at runtime).
            resolveFilterPredicate(predicate, UnknownType.INSTANCE, node);
        }

        private ResolvedType resolveCollectionFunction(
                PropertyChainNode.CollectionFunctionStep step,
                ResolvedType targetType,
                PropertyChainNode node) {
            List<ResolvedType> argTypes = step.arguments().stream()
                    .map(this::resolveExpression).toList();
            Collection<FunctionDescriptor> candidates = context.functionCatalog().findCandidates(step.name());
            if (candidates.isEmpty()) {
                error("UNKNOWN_COLLECTION_FUNCTION",
                        "no function '" + step.name() + "' found in catalog for collection/map call",
                        node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            int expectedArity = step.arguments().size() + 1; // +1 for implicit target
            FunctionDescriptor match = null;
            for (FunctionDescriptor d : candidates) {
                if (d.arity() != expectedArity) continue;
                // Check first param compatibility (target) — lenient: unknown passes
                ResolvedType firstParam = d.parameterResolvedTypes().isEmpty()
                        ? UnknownType.INSTANCE : d.parameterResolvedTypes().getFirst();
                if (firstParam != UnknownType.INSTANCE
                        && !(firstParam == VectorType.INSTANCE)
                        && !(firstParam instanceof CollectionType)
                        && targetType instanceof ObjectType) {
                    continue;
                }
                List<ResolvedType> explicitArgTypes = d.parameterResolvedTypes().subList(1, d.parameterResolvedTypes().size());
                if (!matchesArgList(explicitArgTypes, argTypes)) continue;
                if (match != null) {
                    error("AMBIGUOUS_FUNCTION",
                            "ambiguous collection function call '" + step.name() + "'", node.sourceSpan());
                    return UnknownType.INSTANCE;
                }
                match = d;
            }
            if (match == null) {
                error("INCOMPATIBLE_COLLECTION_FUNCTION_ARGUMENTS",
                        "no compatible overload for collection function '" + step.name() + "'",
                        node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            ResolvedFunctionBinding binding = new ResolvedFunctionBinding(match.functionRef(), match, match.returnType());
            functionBindings.put(node.nodeId(), binding);
            return match.returnType();
        }

        private boolean matchesArgList(List<ResolvedType> expectedTypes, List<ResolvedType> actualTypes) {
            if (expectedTypes.size() != actualTypes.size()) return false;
            for (int i = 0; i < expectedTypes.size(); i++) {
                ResolvedType expected = expectedTypes.get(i);
                ResolvedType actual = actualTypes.get(i);
                if (actual == NullType.INSTANCE || expected == NullType.INSTANCE) continue;
                if (actual != UnknownType.INSTANCE && expected != UnknownType.INSTANCE && !actual.equals(expected)) {
                    return false;
                }
            }
            return true;
        }

        private ResolvedType resolveRootType(PropertyChainNode node) {
            if (CURRENT_ELEMENT.equals(node.rootIdentifier())) {
                // '@' as root is only valid in filter predicates — handled by resolvePropertyChain
                return filterElementType != null ? filterElementType : UnknownType.INSTANCE;
            }
            SymbolRef symbolRef = internalSymbolsByName.get(node.rootIdentifier());
            if (symbolRef != null) {
                symbolByNodeId.put(node.nodeId(), symbolRef);
                return internalTypes.getOrDefault(symbolRef, UnknownType.INSTANCE);
            }

            symbolRef = externalSymbolsByName.computeIfAbsent(
                    node.rootIdentifier(), name -> new SymbolRef(name, SymbolKind.EXTERNAL));
            symbolByNodeId.put(node.nodeId(), symbolRef);
            return context.externalSymbolCatalog().find(node.rootIdentifier())
                    .map(ExternalSymbolDescriptor::declaredType)
                    .orElse(UnknownType.INSTANCE);
        }

        private ResolvedType resolveProperty(
                TypeMetadata metadata,
                PropertyChainNode.PropertyAccess access,
                SourceSpan sourceSpan) {
            PropertyDescriptor descriptor = metadata.properties().get(access.name());
            if (descriptor == null) {
                error("UNKNOWN_PROPERTY",
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
                error("UNKNOWN_METHOD",
                        "method '" + access.name() + "' not found on " + metadata.javaClass().getSimpleName(),
                        sourceSpan);
                return UnknownType.INSTANCE;
            }

            int expectedArity = access.arguments().size();
            List<MethodDescriptor> arityMatches = candidates.stream()
                    .filter(candidate -> candidate.arity() == expectedArity)
                    .toList();
            if (arityMatches.isEmpty()) {
                error("INVALID_METHOD_ARITY",
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
                    error("AMBIGUOUS_METHOD",
                            "ambiguous method call '" + access.name() + "' on " + metadata.javaClass().getSimpleName(),
                            sourceSpan);
                    return UnknownType.INSTANCE;
                }
                exactMatch = candidate;
            }
            if (exactMatch == null) {
                error("INCOMPATIBLE_METHOD_ARGUMENTS",
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
            return context.typeHintCatalog().isRegistered(javaType)
                    ? new ObjectType(javaType)
                    : ResolvedTypes.fromJavaType(javaType);
        }

        private ResolvedType resolveFunctionCall(FunctionCallNode node) {
            List<ResolvedType> argumentTypes = node.arguments().stream()
                .map(this::resolveExpression)
                .toList();
            Collection<FunctionDescriptor> candidates = context.functionCatalog().findCandidates(node.functionName());
            if (candidates.isEmpty()) {
                error("UNKNOWN_FUNCTION", "unknown function '" + node.functionName() + "'", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            int expectedArity = node.arguments().size();
            FunctionDescriptor exactMatch = null;
            boolean arityFound = false;
            for (FunctionDescriptor d : candidates) {
                if (d.arity() != expectedArity) continue;
                arityFound = true;
                if (!matchesArguments(d, argumentTypes)) continue;
                if (exactMatch != null) {
                    error("AMBIGUOUS_FUNCTION", "ambiguous function call '" + node.functionName() + "'", node.sourceSpan());
                    return UnknownType.INSTANCE;
                }
                exactMatch = d;
            }
            if (!arityFound) {
                error("INVALID_FUNCTION_ARITY", "invalid arity for function '" + node.functionName() + "'", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            if (exactMatch == null) {
                error("INCOMPATIBLE_FUNCTION_ARGUMENTS", "incompatible arguments for function '" + node.functionName() + "'", node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            ResolvedFunctionBinding binding = new ResolvedFunctionBinding(exactMatch.functionRef(), exactMatch, exactMatch.returnType());
            functionBindings.put(node.nodeId(), binding);
            return exactMatch.returnType();
        }

        private boolean matchesArguments(FunctionDescriptor descriptor, List<ResolvedType> argumentTypes) {
            for (int index = 0; index < argumentTypes.size(); index++) {
                ResolvedType actualType = argumentTypes.get(index);
                ResolvedType expectedType = descriptor.parameterResolvedTypes().get(index);
                if (actualType == NullType.INSTANCE || expectedType == NullType.INSTANCE) {
                    continue;
                }
                if (actualType != UnknownType.INSTANCE && expectedType != UnknownType.INSTANCE && !actualType.equals(expectedType)) {
                    return false;
                }
            }
            return true;
        }

        private ResolvedType resolveConditional(ConditionalNode node) {
            node.conditions().forEach(condition -> expectType(resolveExpression(condition), ScalarType.BOOLEAN, "condition", condition.sourceSpan()));
            ResolvedType resultType = UnknownType.INSTANCE;
            for (ExpressionNode result : node.results()) {
                resultType = ResolvedTypes.merge(resultType, resolveExpression(result));
            }
            return ResolvedTypes.merge(resultType, resolveExpression(node.elseExpression()));
        }

        private ResolvedType resolveUnary(UnaryOperationNode node) {
            ResolvedType operandType = resolveExpression(node.operand());
            return switch (node.operator()) {
                case NEGATE -> expectType(operandType, ScalarType.NUMBER, "unary negate", node.sourceSpan());
                case LOGICAL_NOT -> expectType(operandType, ScalarType.BOOLEAN, "logical not", node.sourceSpan());
                case SQRT, MODULUS -> expectType(operandType, ScalarType.NUMBER, "numeric unary operator", node.sourceSpan());
            };
        }

        private ResolvedType resolveBinary(BinaryOperationNode node) {
            ResolvedType leftType = resolveExpression(node.left());
            ResolvedType rightType = resolveExpression(node.right());
            return switch (node.operator()) {
                case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, POWER, ROOT ->
                    arithmeticType(leftType, rightType, node.sourceSpan());
                case AND, OR, XOR, XNOR, NAND, NOR -> {
                    expectType(leftType, ScalarType.BOOLEAN, "logical operator", node.left().sourceSpan());
                    yield expectType(rightType, ScalarType.BOOLEAN, "logical operator", node.right().sourceSpan());
                }
                case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, EQUAL, NOT_EQUAL -> {
                    if (!compatibleComparison(leftType, rightType)) {
                        error("INCOMPATIBLE_COMPARISON", "comparison uses incompatible operand types", node.sourceSpan());
                    }
                    yield ScalarType.BOOLEAN;
                }
                case NULL_COALESCE -> ResolvedTypes.merge(leftType, rightType);
                case CONCATENATE -> {
                    expectType(leftType, ScalarType.STRING, "string concatenation", node.left().sourceSpan());
                    yield expectType(rightType, ScalarType.STRING, "string concatenation", node.right().sourceSpan());
                }
                case REGEX_MATCH, REGEX_NOT_MATCH -> {
                    expectType(leftType, ScalarType.STRING, "regex match subject", node.left().sourceSpan());
                    expectType(rightType, ScalarType.STRING, "regex match pattern", node.right().sourceSpan());
                    yield ScalarType.BOOLEAN;
                }
                case IN, NOT_IN -> {
                    if (rightType != UnknownType.INSTANCE
                            && rightType != VectorType.INSTANCE
                            && !(rightType instanceof CollectionType)) {
                        error("INCOMPATIBLE_IN_OPERANDS",
                              "membership operator expects a collection/vector right operand but found " + rightType,
                              node.right().sourceSpan());
                    }
                    yield ScalarType.BOOLEAN;
                }
            };
        }

        private ResolvedType resolveTernary(TernaryOperationNode node) {
            ResolvedType valueType = resolveExpression(node.first());
            ResolvedType lowerType = resolveExpression(node.second());
            ResolvedType upperType = resolveExpression(node.third());
            if (!compatibleComparison(valueType, lowerType) || !compatibleComparison(valueType, upperType)) {
                error("INCOMPATIBLE_COMPARISON", "between operator uses incompatible operand types", node.sourceSpan());
            }
            return ScalarType.BOOLEAN;
        }

        private ResolvedType resolvePostfix(PostfixOperationNode node) {
            return expectType(resolveExpression(node.operand()), ScalarType.NUMBER, "postfix operator", node.sourceSpan());
        }

        private ResolvedType resolveVector(VectorLiteralNode node) {
            node.elements().forEach(this::resolveExpression);
            return VectorType.INSTANCE;
        }

        private ResolvedType arithmeticType(ResolvedType leftType, ResolvedType rightType, SourceSpan sourceSpan) {
            expectType(leftType, ScalarType.NUMBER, "arithmetic operator", sourceSpan);
            expectType(rightType, ScalarType.NUMBER, "arithmetic operator", sourceSpan);
            return ScalarType.NUMBER;
        }

        private ResolvedType expectType(
            ResolvedType actualType,
            ScalarType expectedType,
            String operation,
            SourceSpan sourceSpan
        ) {
            if (actualType != UnknownType.INSTANCE && actualType != NullType.INSTANCE && actualType != expectedType) {
                error("TYPE_MISMATCH", operation + " expects " + expectedType + " but found " + actualType, sourceSpan);
                return UnknownType.INSTANCE;
            }
            return expectedType;
        }

        private boolean compatibleComparison(ResolvedType leftType, ResolvedType rightType) {
            if (leftType == UnknownType.INSTANCE || rightType == UnknownType.INSTANCE) {
                return true;
            }
            if (leftType == NullType.INSTANCE || rightType == NullType.INSTANCE) {
                return true;
            }
            return leftType.equals(rightType);
        }

        private void registerInternal(String name) {
            internalSymbolsByName.computeIfAbsent(name, ignored -> new SymbolRef(name, SymbolKind.INTERNAL));
        }

        private void error(String code, String message, SourceSpan sourceSpan) {
            issues.add(new SemanticIssue(code, SemanticIssueSeverity.ERROR, message, sourceSpan));
        }

        private boolean canParseDate(String value) {
            try {
                LocalDate.parse(value);
                return true;
            } catch (DateTimeParseException ignored) {
                return false;
            }
        }

        private boolean canParseTime(String value) {
            try {
                LocalTime.parse(value);
                return true;
            } catch (DateTimeParseException ignored) {
                return false;
            }
        }

        private boolean canParseDateTime(String value) {
            try {
                if (value.contains("+") || value.endsWith("Z")) {
                    OffsetDateTime.parse(value);
                } else {
                    LocalDateTime.parse(value);
                }
                return true;
            } catch (DateTimeParseException ignored) {
                return false;
            }
        }
    }
}
