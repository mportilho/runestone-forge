package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.internal.ast.AssignmentNode;
import com.runestone.expeval_mk3.internal.ast.AssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.BetweenNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.CallArgument;
import com.runestone.expeval_mk3.internal.ast.CallNavigationLink;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalNode;
import com.runestone.expeval_mk3.internal.ast.CurrentItemNode;
import com.runestone.expeval_mk3.internal.ast.CurrentTemporalValueNode;
import com.runestone.expeval_mk3.internal.ast.DestructuringAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionCallArgument;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.FilterNavigationLink;
import com.runestone.expeval_mk3.internal.ast.FunctionCallNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.IndexSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.LambdaCallArgument;
import com.runestone.expeval_mk3.internal.ast.LambdaNode;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.MembershipNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperator;
import com.runestone.expeval_mk3.internal.ast.PostfixOperatorOccurrence;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.StringKeySubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SubscriptBounds;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.WildcardNavigationLink;
import com.runestone.expeval_mk3.internal.runtime.BetweenExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.BinaryExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CollectionLiteralExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutor;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationExecutors;
import com.runestone.expeval_mk3.internal.runtime.CollectionOperationRuntimeBinding;
import com.runestone.expeval_mk3.internal.runtime.ConditionalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ConstantExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ConstantFolder;
import com.runestone.expeval_mk3.internal.runtime.ContextualMemberExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CurrentTemporalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutableBranch;
import com.runestone.expeval_mk3.internal.runtime.ExecutableLambda;
import com.runestone.expeval_mk3.internal.runtime.ExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutableOperationArguments;
import com.runestone.expeval_mk3.internal.runtime.FilterExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.FrameReadExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.FunctionCallExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.IndexSubscriptExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.MapKeySubscriptExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.MembershipExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.NullCoalesceExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.PostfixExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.RegisteredMethodExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.RegisteredPropertyExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.SliceSubscriptExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.UnaryExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.WildcardExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationBinding;
import com.runestone.expeval_mk3.internal.semantics.ContextualMemberNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.DeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.DestructuringMinimumSizeDeferredCheck;
import com.runestone.expeval_mk3.internal.semantics.FilterNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.IndexSubscriptNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.MapKeySubscriptNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.NavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredMethodNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredPropertyNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SliceSubscriptNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.SymbolBinding;
import com.runestone.expeval_mk3.internal.semantics.WildcardNavigationBinding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Builds the immutable {@link ExecutionPlan} from a successful {@code SemanticModel}, in one of two
 * modes selected by a single internal {@code folding} field: {@link #build} produces the optimized plan
 * and {@link #buildOracle} produces the Unoptimized Oracle that {@code build} is validated against (ADR
 * 0019). The transformation happens during construction, not as a rewrite pass over a built
 * {@link ExecutableNode} tree: that family of nodes has no traversal or reconstruction protocol, and
 * adding one would only pay to rebuild what this builder already built with the full semantic metadata
 * in hand. Every violation of the Etapa 4 completeness contract (missing type, binding, prepared value,
 * or numeric fact) is an internal bug in this builder, never a late user diagnostic; it fails as an
 * {@link IllegalStateException} instead of inferring, resolving, or rediscovering the missing decision.
 */
public final class ExecutionPlanBuilder {

    private final boolean folding;

    public ExecutionPlanBuilder() {
        this(true);
    }

    private ExecutionPlanBuilder(boolean folding) {
        this.folding = folding;
    }

    /**
     * Builds the optimized plan that the public compilation entry point uses.
     */
    public ExecutionPlan build(SemanticModel model, ExpressionEnvironment environment) {
        return withFolding(true).buildPlan(model, environment);
    }

    /**
     * Builds the Unoptimized Oracle from the same pipeline as {@link #build}, so that any difference
     * between the two forms can only come from an optimization and never from a second implementation
     * drifting away. Selection is internal to the module: there is no public flag, system property, or
     * duplicated runtime built on this path.
     */
    ExecutionPlan buildOracle(SemanticModel model, ExpressionEnvironment environment) {
        return withFolding(false).buildPlan(model, environment);
    }

    /**
     * Returns {@code this} when it already carries the requested mode, and a fresh instance with that
     * mode otherwise; keeps {@code folding} the single, genuinely consulted mode selector rather than a
     * field one entry point writes and the other ignores.
     */
    private ExecutionPlanBuilder withFolding(boolean folding) {
        return this.folding == folding ? this : new ExecutionPlanBuilder(folding);
    }

    private ExecutionPlan buildPlan(SemanticModel model, ExpressionEnvironment environment) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(environment, "environment");
        Map<NodeId, List<DeferredCheck>> deferredChecksByNode = model.deferredChecks().stream()
                .collect(Collectors.groupingBy(DeferredCheck::nodeId));
        List<FoldedRead> foldedReads = new ArrayList<>();
        ExecutableNode result = model.ast().resultExpression()
                .map(expression -> buildNode(expression, model, environment, deferredChecksByNode, foldedReads))
                .orElse(null);
        ExpressionType resultType = model.ast().resultExpression()
                .map(expression -> required(model.resolvedTypes(), expression.id(), "result expression type"))
                .orElse(null);
        List<ExternalBindingPlan> externalBindings = model.frameLayout().externalBindings().stream()
                .map(binding -> new ExternalBindingPlan(binding.requireExternalSymbol(), binding.frameSlot()))
                .toList();
        Set<String> usedSymbolNames = externalBindings.stream()
                .map(binding -> binding.symbol().name())
                .collect(Collectors.toSet());
        List<ExternalSymbol> declaredSymbolsInCanonicalOrder = new ArrayList<>(externalBindings.size());
        externalBindings.forEach(binding -> declaredSymbolsInCanonicalOrder.add(binding.symbol()));
        environment.externalSymbols().values().stream()
                .filter(symbol -> !usedSymbolNames.contains(symbol.name()))
                .forEach(declaredSymbolsInCanonicalOrder::add);
        List<AssignmentExecutable> assignments = model.ast().assignments().stream()
                .map(assignment -> buildAssignment(assignment, model, environment, deferredChecksByNode, foldedReads))
                .toList();
        List<AssignedSymbol> assignedSymbolsInCreationOrder = buildAssignedSymbolsInCreationOrder(model);
        return new ExecutionPlan(
                result,
                resultType,
                assignments,
                externalBindings,
                declaredSymbolsInCanonicalOrder,
                assignedSymbolsInCreationOrder,
                foldedReads,
                model.frameLayout().frameSize(),
                environment.boundaryCoercion(),
                environment.zoneId(),
                environment.maxMaterializedSize());
    }

    /**
     * Walks assignment targets in source order, keeping only each internal symbol's first occurrence:
     * reassignment reuses the same {@code SymbolBinding} (frame slot and type), so later occurrences of an
     * already-seen name contribute nothing new to the ordering.
     */
    private List<AssignedSymbol> buildAssignedSymbolsInCreationOrder(SemanticModel model) {
        Map<String, AssignedSymbol> byName = new LinkedHashMap<>();
        for (AssignmentNode assignment : model.ast().assignments()) {
            for (IdentifierAssignmentTargetNode identifier : targetIdentifiers(assignment.target())) {
                byName.computeIfAbsent(identifier.name(), name -> {
                    SymbolBinding binding = required(model.symbolBindings(), identifier.id(), "assignment target binding");
                    return new AssignedSymbol(name, binding.type(), binding.frameSlot(), identifier.sourceSpan());
                });
            }
        }
        return List.copyOf(byName.values());
    }

    private static List<IdentifierAssignmentTargetNode> targetIdentifiers(AssignmentTargetNode target) {
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            return List.of(identifier);
        }
        if (target instanceof DestructuringAssignmentTargetNode destructuring) {
            return destructuring.elements();
        }
        throw new IllegalArgumentException("unsupported assignment target: " + target.getClass().getSimpleName());
    }

    private AssignmentExecutable buildAssignment(
            AssignmentNode assignment,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        ExecutableNode expression = buildNode(assignment.expression(), model, environment, deferredChecksByNode, foldedReads);
        AssignmentTargetNode target = assignment.target();
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            SymbolBinding binding = required(model.symbolBindings(), identifier.id(), "assignment target binding");
            return AssignmentExecutable.identifier(target.id(), target.sourceSpan(), binding.frameSlot(), expression);
        }
        if (target instanceof DestructuringAssignmentTargetNode destructuring) {
            int[] frameSlots = destructuring.elements().stream()
                    .mapToInt(element -> required(model.symbolBindings(), element.id(), "assignment target binding")
                            .frameSlot())
                    .toArray();
            DestructuringMinimumSizeDeferredCheck minimumSizeCheck = deferredChecksByNode
                    .getOrDefault(destructuring.id(), List.of()).stream()
                    .filter(DestructuringMinimumSizeDeferredCheck.class::isInstance)
                    .map(DestructuringMinimumSizeDeferredCheck.class::cast)
                    .findFirst()
                    .orElse(null);
            return AssignmentExecutable.destructuring(
                    target.id(), target.sourceSpan(), frameSlots, expression, minimumSizeCheck);
        }
        throw new IllegalArgumentException("unsupported assignment target: " + target.getClass().getSimpleName());
    }

    private ExecutableNode buildNode(
            ExpressionNode node,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        return switch (node) {
            case LiteralNode literal -> new ConstantExecutableNode(
                    literal.id(), literal.sourceSpan(),
                    required(model.preparedValues(), literal.id(), "prepared literal value"));
            case CollectionLiteralNode collection -> {
                List<ExecutableNode> elements = collection.elements().stream()
                        .map(element -> buildNode(element, model, environment, deferredChecksByNode, foldedReads))
                        .toList();
                yield fold(new CollectionLiteralExecutableNode(collection.id(), collection.sourceSpan(), elements),
                        elements.toArray(ExecutableNode[]::new));
            }
            case IdentifierNode identifier -> buildIdentifierRead(identifier, model, foldedReads);
            case CurrentItemNode currentItem -> new FrameReadExecutableNode(
                    currentItem.id(), currentItem.sourceSpan(),
                    required(model.symbolBindings(), currentItem.id(), "current item binding").frameSlot());
            case CurrentTemporalValueNode currentTemporalValue -> new CurrentTemporalExecutableNode(
                    currentTemporalValue.id(), currentTemporalValue.sourceSpan(), currentTemporalValue.kind());
            case GroupedExpressionNode grouped ->
                    buildNode(grouped.expression(), model, environment, deferredChecksByNode, foldedReads);
            case BinaryOperationNode binary ->
                    buildBinary(binary, model, requireEnvironment(environment), deferredChecksByNode, foldedReads);
            case UnaryOperationNode unary -> {
                ExecutableNode operand = buildNode(unary.operand(), model, environment, deferredChecksByNode, foldedReads);
                UnaryExecutableNode built = new UnaryExecutableNode(unary.id(), unary.sourceSpan(), unary.operator(), operand);
                ExecutableNode doubleNegationElided = foldDoubleNegation(built);
                yield doubleNegationElided != built ? doubleNegationElided : fold(built, operand);
            }
            case PostfixOperationNode postfix ->
                    buildPostfix(postfix, model, requireEnvironment(environment), deferredChecksByNode, foldedReads);
            case BetweenNode between -> buildBetween(between, model, environment, deferredChecksByNode, foldedReads);
            case MembershipNode membership -> buildMembership(membership, model, environment, deferredChecksByNode, foldedReads);
            case NullCoalesceNode coalesce -> foldNullCoalesce(new NullCoalesceExecutableNode(
                    coalesce.id(), coalesce.sourceSpan(),
                    coalesce.operands().stream()
                            .map(operand -> buildNode(operand, model, environment, deferredChecksByNode, foldedReads))
                            .toList()));
            case ConditionalNode conditional -> buildConditional(conditional, model, environment, deferredChecksByNode, foldedReads);
            case FunctionCallNode functionCall -> buildFunctionCall(functionCall, model, environment, deferredChecksByNode, foldedReads);
            case NavigationChainNode navigation -> buildNavigationChain(navigation, model, environment, deferredChecksByNode, foldedReads);
        };
    }

    /**
     * Folds a non-overridable External Symbol read to its validated environment default (ADR 0019,
     * issue #117) and records the fold as a {@link FoldedRead}; an overridable symbol, or an internal
     * symbol, always reads its frame slot. This is the only symbol read Etapa 7 authorizes to fold.
     */
    private ExecutableNode buildIdentifierRead(
            IdentifierNode identifier, SemanticModel model, List<FoldedRead> foldedReads) {
        SymbolBinding binding = required(model.symbolBindings(), identifier.id(), "symbol binding");
        if (folding && binding.external()
                && binding.requireExternalSymbol().overwritePolicy() == ExternalSymbolOverwritePolicy.FIXED) {
            Object value = binding.requireExternalSymbol().defaultValue().value();
            foldedReads.add(new FoldedRead(identifier.name(), identifier.id(), identifier.sourceSpan(), value));
            return new ConstantExecutableNode(identifier.id(), identifier.sourceSpan(), value);
        }
        return new FrameReadExecutableNode(identifier.id(), identifier.sourceSpan(), binding.frameSlot());
    }

    private ExecutableNode buildBinary(
            BinaryOperationNode binary,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        ExecutableNode left = buildNode(binary.left(), model, environment, deferredChecksByNode, foldedReads);
        ExecutableNode right = buildNode(binary.right(), model, environment, deferredChecksByNode, foldedReads);
        BinaryOperator operator = binary.operator();
        return switch (operator) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, ROOT, EXPONENTIATE, CONCATENATE -> fold(BinaryExecutableNode.arithmetic(
                    binary.id(), binary.sourceSpan(), operator, left, right, environment.mathContext(),
                    deferredChecksByNode.getOrDefault(binary.id(), List.of())), left, right);
            case LOGICAL_AND, LOGICAL_OR, LOGICAL_NAND, LOGICAL_NOR, LOGICAL_XOR, LOGICAL_XNOR -> fold(
                    BinaryExecutableNode.logical(binary.id(), binary.sourceSpan(), operator, left, right), left, right);
            case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL -> fold(BinaryExecutableNode.comparison(
                    binary.id(), binary.sourceSpan(), operator, left, right, model.resolvedTypes().get(binary.left().id())),
                    left, right);
            case EQUAL, NOT_EQUAL -> fold(BinaryExecutableNode.equality(
                    binary.id(), binary.sourceSpan(), operator, left, right,
                    required(model.equalityOperandTypes(), binary.id(), "equality operand type")), left, right);
            case REGEX_MATCH, REGEX_NOT_MATCH -> fold(BinaryExecutableNode.regex(
                    binary.id(), binary.sourceSpan(), operator, left,
                    (Pattern) required(model.preparedValues(), binary.id(), "prepared regex pattern")), left);
        };
    }

    private ExecutableNode buildPostfix(
            PostfixOperationNode postfix,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        ExecutableNode operand = buildNode(postfix.operand(), model, environment, deferredChecksByNode, foldedReads);
        List<PostfixOperator> operators = postfix.operations().stream().map(PostfixOperatorOccurrence::operator).toList();
        return fold(new PostfixExecutableNode(
                postfix.id(), postfix.sourceSpan(), operand, operators, environment.maxFactorialInput(),
                deferredChecksByNode.getOrDefault(postfix.id(), List.of())), operand);
    }

    private ExecutableNode buildBetween(
            BetweenNode between,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        ExecutableNode value = buildNode(between.value(), model, environment, deferredChecksByNode, foldedReads);
        ExecutableNode lowerBound = buildNode(between.lowerBound(), model, environment, deferredChecksByNode, foldedReads);
        ExecutableNode upperBound = buildNode(between.upperBound(), model, environment, deferredChecksByNode, foldedReads);
        return fold(new BetweenExecutableNode(
                between.id(),
                between.sourceSpan(),
                between.negated(),
                value,
                lowerBound,
                upperBound,
                model.resolvedTypes().get(between.value().id())), value, lowerBound, upperBound);
    }

    private ExecutableNode buildMembership(
            MembershipNode membership,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        MembershipExecutableNode built = new MembershipExecutableNode(
                membership.id(),
                membership.sourceSpan(),
                membership.negated(),
                buildNode(membership.element(), model, environment, deferredChecksByNode, foldedReads),
                buildNode(membership.collection(), model, environment, deferredChecksByNode, foldedReads),
                model.resolvedTypes().get(membership.collection().id()));
        return foldMembership(built);
    }

    private ExecutableNode buildConditional(
            ConditionalNode conditional,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        List<ExecutableBranch> branches = conditional.branches().stream()
                .map(branch -> new ExecutableBranch(
                        buildNode(branch.condition(), model, environment, deferredChecksByNode, foldedReads),
                        buildNode(branch.consequence(), model, environment, deferredChecksByNode, foldedReads)))
                .toList();
        ExecutableNode elseExpression =
                buildNode(conditional.elseExpression(), model, environment, deferredChecksByNode, foldedReads);
        return foldConditional(new ConditionalExecutableNode(conditional.id(), conditional.sourceSpan(), branches, elseExpression));
    }

    private ExecutableNode buildFunctionCall(
            FunctionCallNode functionCall,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        FunctionDescriptor descriptor = required(model.functionBindings(), functionCall.id(), "function binding");
        List<ExecutableNode> arguments = functionCall.arguments().stream()
                .map(ExpressionCallArgument.class::cast)
                .map(argument -> buildNode(argument.expression(), model, environment, deferredChecksByNode, foldedReads))
                .toList();
        FunctionCallExecutableNode built = new FunctionCallExecutableNode(
                functionCall.id(), functionCall.sourceSpan(), descriptor, arguments);
        ExecutableNode assertionElided = foldAssertion(built);
        if (assertionElided != built) {
            return assertionElided;
        }
        return descriptor.foldable() ? fold(built, arguments.toArray(ExecutableNode[]::new)) : built;
    }

    private ExecutableNode buildNavigationChain(
            NavigationChainNode navigation,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        ExecutableNode current = buildNode(navigation.receiver(), model, environment, deferredChecksByNode, foldedReads);
        for (NavigationLink link : navigation.links()) {
            current = buildNavigationLink(link, current, model, environment, deferredChecksByNode, foldedReads);
        }
        return current;
    }

    /**
     * Builds one navigation link and, for every link kind except the collection-shaped ones that must
     * write the current-item frame slot to run at all (filter, wildcard, collection operation — never
     * eligible per the Etapa 7 fold table), attempts to fold it (ADR 0019, issue #117): a pure link
     * over an already-constant receiver (and, for a method call, constant arguments) collapses to its
     * value. An impure link never reaches {@link #fold}, so it and every link to its right stay
     * unfolded by construction — the receiver of the next link is then not a
     * {@link ConstantExecutableNode} and {@link #fold} rejects it on that basis alone.
     */
    private ExecutableNode buildNavigationLink(
            NavigationLink link,
            ExecutableNode receiver,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        NavigationBinding binding = required(model.navigationBindings(), link.id(), "navigation binding");
        NodeId id = link.id();
        var span = link.sourceSpan();
        return switch (binding) {
            case IndexSubscriptNavigationBinding indexBinding -> {
                IndexSubscriptNavigationLink index = (IndexSubscriptNavigationLink) link;
                ExecutableNode built = new IndexSubscriptExecutableNode(id, span, receiver, index.index().value(), index.safe());
                yield foldNavigationLink(indexBinding.pure(), built, receiver);
            }
            case SliceSubscriptNavigationBinding sliceBinding -> {
                SliceSubscriptNavigationLink slice = (SliceSubscriptNavigationLink) link;
                ExecutableNode built = new SliceSubscriptExecutableNode(
                        id, span, receiver, SubscriptBounds.rawValue(slice.start()),
                        SubscriptBounds.rawValue(slice.end()), slice.safe(), environment.maxMaterializedSize());
                yield foldNavigationLink(sliceBinding.pure(), built, receiver);
            }
            case MapKeySubscriptNavigationBinding mapKeyBinding -> {
                StringKeySubscriptNavigationLink stringKey = (StringKeySubscriptNavigationLink) link;
                boolean safe = mapKeyBinding.resultNullability() == RuntimeNullability.MAY_BE_NULL;
                ExecutableNode built = new MapKeySubscriptExecutableNode(id, span, receiver, stringKey.key(), safe);
                yield foldNavigationLink(mapKeyBinding.pure(), built, receiver);
            }
            case FilterNavigationBinding filterBinding -> {
                FilterNavigationLink filter = (FilterNavigationLink) link;
                ExecutableNode predicate = buildNode(filter.predicate(), model, environment, deferredChecksByNode, foldedReads);
                yield new FilterExecutableNode(
                        id, span, receiver, filter.safe(), predicate, filterBinding.currentItemFrameSlot(),
                        environment.maxMaterializedSize());
            }
            case ContextualMemberNavigationBinding memberBinding -> {
                boolean safe = memberBinding.resultNullability() == RuntimeNullability.MAY_BE_NULL;
                ExecutableNode built = new ContextualMemberExecutableNode(id, span, receiver, memberBinding.member(), safe);
                yield foldNavigationLink(memberBinding.pure(), built, receiver);
            }
            case RegisteredPropertyNavigationBinding propertyBinding -> {
                boolean safe = propertyBinding.resultNullability() == RuntimeNullability.MAY_BE_NULL;
                ExecutableNode built = new RegisteredPropertyExecutableNode(id, span, receiver, safe, propertyBinding);
                yield foldNavigationLink(propertyBinding.pure(), built, receiver);
            }
            case RegisteredMethodNavigationBinding methodBinding -> {
                CallNavigationLink call = (CallNavigationLink) link;
                boolean safe = methodBinding.resultNullability() == RuntimeNullability.MAY_BE_NULL;
                List<ExecutableNode> arguments = call.arguments().stream()
                        .map(ExpressionCallArgument.class::cast)
                        .map(argument -> buildNode(argument.expression(), model, environment, deferredChecksByNode, foldedReads))
                        .toList();
                ExecutableNode built = new RegisteredMethodExecutableNode(id, span, receiver, safe, methodBinding, arguments);
                ExecutableNode[] requiredConstants = new ExecutableNode[arguments.size() + 1];
                requiredConstants[0] = receiver;
                for (int i = 0; i < arguments.size(); i++) {
                    requiredConstants[i + 1] = arguments.get(i);
                }
                yield foldNavigationLink(methodBinding.pure(), built, requiredConstants);
            }
            case WildcardNavigationBinding wildcardBinding -> {
                WildcardNavigationLink wildcard = (WildcardNavigationLink) link;
                yield new WildcardExecutableNode(
                        id, span, receiver, wildcard.safe(), wildcardBinding, environment.maxMaterializedSize());
            }
            case CollectionOperationBinding operationBinding -> {
                CallNavigationLink call = (CallNavigationLink) link;
                ExecutableOperationArguments arguments =
                        buildOperationArguments(call, operationBinding, model, environment, deferredChecksByNode, foldedReads);
                CollectionOperationExecutor executor = CollectionOperationExecutors.executorFor(operationBinding.identity());
                CollectionOperationRuntimeBinding runtimeBinding = new CollectionOperationRuntimeBinding(
                        operationBinding.receiverType(),
                        operationBinding.identity() == CollectionOperationCatalog.OperationIdentity.SORT_BY
                                ? operationBinding.lambdaBindings().getFirst().resultType()
                                : null);
                yield new CollectionOperationExecutableNode(
                        id, span, receiver, call.safe(), executor, runtimeBinding,
                        environment.mathContext(), environment.maxMaterializedSize(), arguments);
            }
        };
    }

    private ExecutableOperationArguments buildOperationArguments(
            CallNavigationLink call,
            CollectionOperationBinding binding,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads) {
        ArrayList<ExecutableNode> valueArguments = new ArrayList<>();
        ArrayList<ExecutableLambda> lambdaArguments = new ArrayList<>();
        int lambdaIndex = 0;
        for (CallArgument argument : call.arguments()) {
            if (argument instanceof ExpressionCallArgument expressionArgument) {
                valueArguments.add(buildNode(expressionArgument.expression(), model, environment, deferredChecksByNode, foldedReads));
                continue;
            }
            LambdaCallArgument lambdaArgument = (LambdaCallArgument) argument;
            CollectionOperationBinding.LambdaBinding lambdaBinding = binding.lambdaBindings().get(lambdaIndex++);
            LambdaNode lambda = lambdaArgument.lambda();
            lambdaArguments.add(new ExecutableLambda(
                    buildNode(lambda.body(), model, environment, deferredChecksByNode, foldedReads),
                    lambdaBinding.currentItemFrameSlot()));
        }
        return new ExecutableOperationArguments(valueArguments, lambdaArguments);
    }

    /**
     * Attempts eager constant folding (ADR 0019, issue #115) of an eager, non-lazy construct whose
     * required children are named explicitly, since {@code ExecutableNode} has no generic child
     * traversal. In oracle mode this is a no-op: {@code built} is always returned unchanged.
     */
    private ExecutableNode fold(ExecutableNode built, ExecutableNode... requiredConstantChildren) {
        return folding ? ConstantFolder.fold(built, requiredConstantChildren) : built;
    }

    /**
     * Attempts the fold of one navigation link (ADR 0019, issue #117), gated on the link's declared
     * purity in addition to {@link #fold}'s own constant-children check: an impure link must never be
     * executed at plan-build time, even when its receiver and arguments already are constant, because
     * running it now instead of at execution could observe or produce an effect the Unoptimized Oracle
     * would not have observed or produced at the same point.
     */
    private ExecutableNode foldNavigationLink(boolean pure, ExecutableNode built, ExecutableNode... requiredConstantChildren) {
        return pure ? fold(built, requiredConstantChildren) : built;
    }

    /**
     * Attempts the structural fold of {@code ??} (ADR 0019, issue #116). In oracle mode this is a
     * no-op: {@code built} is always returned unchanged.
     */
    private ExecutableNode foldNullCoalesce(NullCoalesceExecutableNode built) {
        return folding ? ConstantFolder.foldNullCoalesce(built) : built;
    }

    /**
     * Attempts the structural fold of the conditional (ADR 0019, issue #116). In oracle mode this is a
     * no-op: {@code built} is always returned unchanged.
     */
    private ExecutableNode foldConditional(ConditionalExecutableNode built) {
        return folding ? ConstantFolder.foldConditional(built) : built;
    }

    /**
     * Attempts the scalar assertion elision (ADR 0019, issue #118). In oracle mode this is a no-op:
     * {@code built} is always returned unchanged.
     */
    private ExecutableNode foldAssertion(FunctionCallExecutableNode built) {
        return folding ? ConstantFolder.foldAssertion(built) : built;
    }

    /**
     * Attempts the double-negation elision (ADR 0019, issue #118). In oracle mode this is a no-op:
     * {@code built} is always returned unchanged.
     */
    private ExecutableNode foldDoubleNegation(UnaryExecutableNode built) {
        return folding ? ConstantFolder.foldDoubleNegation(built) : built;
    }

    /**
     * Attempts the membership download to a sorted array or hash set (ADR 0019, issue #119). In oracle
     * mode this is a no-op: {@code built} is always returned unchanged.
     */
    private ExecutableNode foldMembership(MembershipExecutableNode built) {
        return folding ? ConstantFolder.foldMembership(built) : built;
    }

    private static ExpressionEnvironment requireEnvironment(ExpressionEnvironment environment) {
        return Objects.requireNonNull(environment, "environment");
    }

    private static <K, V> V required(Map<K, V> values, K key, String description) {
        V value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("semantic model is missing " + description + " for " + key);
        }
        return value;
    }
}
