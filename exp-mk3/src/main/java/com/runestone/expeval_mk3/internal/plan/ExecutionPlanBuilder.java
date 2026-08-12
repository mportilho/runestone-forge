package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CollectionOperationCatalog;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.ExternalSymbol;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
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
import com.runestone.expeval_mk3.internal.runtime.MemoizedExecutableNode;
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
import com.runestone.expeval_mk3.internal.semantics.NumericFact;
import com.runestone.expeval_mk3.internal.semantics.PowerRealDomainDeferredCheck;
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
 * modes selected by a single internal {@code optimizing} field: {@link #build} produces the optimized
 * plan and {@link #buildOracle} produces the Unoptimized Oracle that {@code build} is validated against
 * (ADR 0019). {@code optimizing} gates every transformation this builder is authorized to apply on top
 * of the metadata {@code SemanticModel} already computed and validated — Dobra de Constante today, and
 * specialized-node choice in later Etapa 8 tickets. The transformation happens during construction, not
 * as a rewrite pass over a built {@link ExecutableNode} tree: that family of nodes has no traversal or
 * reconstruction protocol, and adding one would only pay to rebuild what this builder already built with
 * the full semantic metadata in hand. Every violation of the Etapa 4 completeness contract (missing type,
 * binding, prepared value, or numeric fact) is an internal bug in this builder, never a late user
 * diagnostic; it fails as an {@link IllegalStateException} instead of inferring, resolving, or
 * rediscovering the missing decision.
 *
 * <p>Etapa 8 entry gate (issue #124), each item verified before any specialization work in this class:
 * resolved type and Runtime Nullability present for every operand node — read via {@link #buildNode}'s
 * {@code BindingLookup.required} lookup into {@code model.runtimeNullability()}, proven by
 * {@code ExecutionPlanBuilderNavigationSeamTest#planBuildingFailsInsteadOfRederivingMissingRuntimeNullability};
 * Numeric Fact present for every {@code NUMBER} node, consumed by {@link #powerDomainProven} rather than
 * merely checked, proven by {@code RealDomainArithmeticTest}'s {@code domainProven*} cases; every
 * {@code PowerRealDomainDeferredCheck} has a runtime consumer, proven the same way; purity recorded for
 * every node and navigation link, read throughout via {@code binding.pure()}, proven by
 * {@code SemanticModelCompletenessGateTest}; canonical Frame Layout with memo slots appended past
 * {@code frameSize}, proven by {@code CommonSubexpressionAnalyzer}'s frame layout tests; single origin
 * for the {@code safe} bit between this builder and {@link CommonSubexpressionAnalyzer}, both now
 * routed through {@link NavigationBinding#safe()}. The remaining gate items —
 * resolved type on every node and the Etapa 4 completeness contract as a whole — are proven exhaustively
 * by {@code SemanticModelCompletenessGateTest} at the {@code SemanticModel} construction boundary this
 * builder always receives already validated.
 */
public final class ExecutionPlanBuilder {

    private final boolean optimizing;

    public ExecutionPlanBuilder() {
        this(true);
    }

    private ExecutionPlanBuilder(boolean optimizing) {
        this.optimizing = optimizing;
    }

    /**
     * Builds the optimized plan that the public compilation entry point uses.
     */
    public ExecutionPlan build(SemanticModel model, ExpressionEnvironment environment) {
        return withOptimizing(true).buildPlan(model, environment);
    }

    /**
     * Builds the Unoptimized Oracle from the same pipeline as {@link #build}, so that any difference
     * between the two forms can only come from an optimization and never from a second implementation
     * drifting away. Selection is internal to the module: there is no public flag, system property, or
     * duplicated runtime built on this path.
     */
    ExecutionPlan buildOracle(SemanticModel model, ExpressionEnvironment environment) {
        return withOptimizing(false).buildPlan(model, environment);
    }

    /**
     * Returns {@code this} when it already carries the requested mode, and a fresh instance with that
     * mode otherwise; keeps {@code optimizing} the single, genuinely consulted mode selector rather than
     * a field one entry point writes and the other ignores.
     */
    private ExecutionPlanBuilder withOptimizing(boolean optimizing) {
        return this.optimizing == optimizing ? this : new ExecutionPlanBuilder(optimizing);
    }

    private ExecutionPlan buildPlan(SemanticModel model, ExpressionEnvironment environment) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(environment, "environment");
        Map<NodeId, List<DeferredCheck>> deferredChecksByNode = model.deferredChecks().stream()
                .collect(Collectors.groupingBy(DeferredCheck::nodeId));
        List<FoldedRead> foldedReads = new ArrayList<>();
        CommonSubexpressionAnalysis commonSubexpressions =
                optimizing ? CommonSubexpressionAnalyzer.analyze(model) : CommonSubexpressionAnalysis.EMPTY;
        Map<NodeId, Integer> memoSlots = commonSubexpressions.memoSlotsByNodeId();
        ExecutableNode result = model.ast().resultExpression()
                .map(expression -> buildNode(expression, model, environment, deferredChecksByNode, foldedReads, memoSlots))
                .orElse(null);
        ExpressionType resultType = model.ast().resultExpression()
                .map(expression -> BindingLookup.required(model.resolvedTypes(), expression.id(), "result expression type"))
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
                .map(assignment -> buildAssignment(assignment, model, environment, deferredChecksByNode, foldedReads, memoSlots))
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
                model.frameLayout().frameSize() + commonSubexpressions.memoSlotCount(),
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
                    SymbolBinding binding = BindingLookup.required(model.symbolBindings(), identifier.id(), "assignment target binding");
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
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        ExecutableNode expression = buildNode(assignment.expression(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
        AssignmentTargetNode target = assignment.target();
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            SymbolBinding binding = BindingLookup.required(model.symbolBindings(), identifier.id(), "assignment target binding");
            return AssignmentExecutable.identifier(target.id(), target.sourceSpan(), binding.frameSlot(), expression);
        }
        if (target instanceof DestructuringAssignmentTargetNode destructuring) {
            int[] frameSlots = destructuring.elements().stream()
                    .mapToInt(element -> BindingLookup.required(model.symbolBindings(), element.id(), "assignment target binding")
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
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        BindingLookup.required(model.runtimeNullability(), node.id(), "runtime nullability");
        return memoize(node.id(), buildNodeWithoutMemo(node, model, environment, deferredChecksByNode, foldedReads, memoSlots), memoSlots);
    }

    private ExecutableNode buildNodeWithoutMemo(
            ExpressionNode node,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        return switch (node) {
            case LiteralNode literal -> new ConstantExecutableNode(
                    literal.id(), literal.sourceSpan(),
                    BindingLookup.required(model.preparedValues(), literal.id(), "prepared literal value"));
            case CollectionLiteralNode collection -> {
                List<ExecutableNode> elements = collection.elements().stream()
                        .map(element -> buildNode(element, model, environment, deferredChecksByNode, foldedReads, memoSlots))
                        .toList();
                yield fold(new CollectionLiteralExecutableNode(collection.id(), collection.sourceSpan(), elements),
                        elements.toArray(ExecutableNode[]::new));
            }
            case IdentifierNode identifier -> buildIdentifierRead(identifier, model, foldedReads);
            case CurrentItemNode currentItem -> new FrameReadExecutableNode(
                    currentItem.id(), currentItem.sourceSpan(),
                    BindingLookup.required(model.symbolBindings(), currentItem.id(), "current item binding").frameSlot());
            case CurrentTemporalValueNode currentTemporalValue -> new CurrentTemporalExecutableNode(
                    currentTemporalValue.id(), currentTemporalValue.sourceSpan(), currentTemporalValue.kind());
            case GroupedExpressionNode grouped ->
                    buildNode(grouped.expression(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
            case BinaryOperationNode binary ->
                    buildBinary(binary, model, requireEnvironment(environment), deferredChecksByNode, foldedReads, memoSlots);
            case UnaryOperationNode unary -> {
                ExecutableNode operand = buildNode(unary.operand(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
                UnaryExecutableNode built = new UnaryExecutableNode(unary.id(), unary.sourceSpan(), unary.operator(), operand);
                ExecutableNode doubleNegationElided = foldDoubleNegation(built);
                yield doubleNegationElided != built ? doubleNegationElided : fold(built, operand);
            }
            case PostfixOperationNode postfix ->
                    buildPostfix(postfix, model, requireEnvironment(environment), deferredChecksByNode, foldedReads, memoSlots);
            case BetweenNode between -> buildBetween(between, model, environment, deferredChecksByNode, foldedReads, memoSlots);
            case MembershipNode membership -> buildMembership(membership, model, environment, deferredChecksByNode, foldedReads, memoSlots);
            case NullCoalesceNode coalesce -> foldNullCoalesce(new NullCoalesceExecutableNode(
                    coalesce.id(), coalesce.sourceSpan(),
                    coalesce.operands().stream()
                            .map(operand -> buildNode(operand, model, environment, deferredChecksByNode, foldedReads, memoSlots))
                            .toList()));
            case ConditionalNode conditional -> buildConditional(conditional, model, environment, deferredChecksByNode, foldedReads, memoSlots);
            case FunctionCallNode functionCall -> buildFunctionCall(functionCall, model, environment, deferredChecksByNode, foldedReads, memoSlots);
            case NavigationChainNode navigation -> buildNavigationChain(navigation, model, environment, deferredChecksByNode, foldedReads, memoSlots);
        };
    }

    /**
     * Folds a non-overridable External Symbol read to its validated environment default (ADR 0019,
     * issue #117) and records the fold as a {@link FoldedRead}; an overridable symbol, or an internal
     * symbol, always reads its frame slot. This is the only symbol read Etapa 7 authorizes to fold.
     */
    private ExecutableNode buildIdentifierRead(
            IdentifierNode identifier, SemanticModel model, List<FoldedRead> foldedReads) {
        SymbolBinding binding = BindingLookup.required(model.symbolBindings(), identifier.id(), "symbol binding");
        if (optimizing && binding.external()
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
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        ExecutableNode left = buildNode(binary.left(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
        ExecutableNode right = buildNode(binary.right(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
        BinaryOperator operator = binary.operator();
        return switch (operator) {
            case ADD, SUBTRACT, MULTIPLY, DIVIDE, MODULO, ROOT, EXPONENTIATE, CONCATENATE -> fold(BinaryExecutableNode.arithmetic(
                    binary.id(), binary.sourceSpan(), operator, left, right, environment.mathContext(),
                    deferredChecksByNode.getOrDefault(binary.id(), List.of()),
                    powerDomainProven(binary, model, deferredChecksByNode)), left, right);
            case LOGICAL_AND, LOGICAL_OR, LOGICAL_NAND, LOGICAL_NOR, LOGICAL_XOR, LOGICAL_XNOR -> fold(
                    BinaryExecutableNode.logical(binary.id(), binary.sourceSpan(), operator, left, right), left, right);
            case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL -> fold(BinaryExecutableNode.comparison(
                    binary.id(), binary.sourceSpan(), operator, left, right, model.resolvedTypes().get(binary.left().id())),
                    left, right);
            case EQUAL, NOT_EQUAL -> fold(BinaryExecutableNode.equality(
                    binary.id(), binary.sourceSpan(), operator, left, right,
                    BindingLookup.required(model.equalityOperandTypes(), binary.id(), "equality operand type")), left, right);
            case REGEX_MATCH, REGEX_NOT_MATCH -> fold(BinaryExecutableNode.regex(
                    binary.id(), binary.sourceSpan(), operator, left,
                    (Pattern) BindingLookup.required(model.preparedValues(), binary.id(), "prepared regex pattern")), left);
        };
    }

    /**
     * Consumes {@code SemanticModel#numericFacts} and the resolver's own {@code PowerRealDomainDeferredCheck}
     * decision (issue #124): {@code true} only when the base's Numeric Fact carries a known strictly
     * positive sign AND the resolver did not emit a deferred check for this node — the exact "proven real
     * for any exponent" branch of {@code classifyPowerDomain}. Reading the AST base's Numeric Fact here,
     * before folding runs, avoids being misled by a base that only becomes a folded constant later (e.g.
     * {@code (2 + 2) ^ x}, whose sum has no known parity even though it folds to a positive constant).
     */
    private static boolean powerDomainProven(
            BinaryOperationNode binary, SemanticModel model, Map<NodeId, List<DeferredCheck>> deferredChecksByNode) {
        if (binary.operator() != BinaryOperator.EXPONENTIATE) {
            return false;
        }
        boolean checkEmitted = deferredChecksByNode.getOrDefault(binary.id(), List.of()).stream()
                .anyMatch(PowerRealDomainDeferredCheck.class::isInstance);
        if (checkEmitted) {
            return false;
        }
        NumericFact baseFact = NumericFact.of(model.numericFacts(), binary.left().id());
        return baseFact.hasParity() && baseFact.parity().signum() > 0;
    }

    private ExecutableNode buildPostfix(
            PostfixOperationNode postfix,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        ExecutableNode operand = buildNode(postfix.operand(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
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
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        ExecutableNode value = buildNode(between.value(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
        ExecutableNode lowerBound = buildNode(between.lowerBound(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
        ExecutableNode upperBound = buildNode(between.upperBound(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
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
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        MembershipExecutableNode built = new MembershipExecutableNode(
                membership.id(),
                membership.sourceSpan(),
                membership.negated(),
                buildNode(membership.element(), model, environment, deferredChecksByNode, foldedReads, memoSlots),
                buildNode(membership.collection(), model, environment, deferredChecksByNode, foldedReads, memoSlots),
                model.resolvedTypes().get(membership.collection().id()));
        return foldMembership(built);
    }

    private ExecutableNode buildConditional(
            ConditionalNode conditional,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        List<ExecutableBranch> branches = conditional.branches().stream()
                .map(branch -> new ExecutableBranch(
                        buildNode(branch.condition(), model, environment, deferredChecksByNode, foldedReads, memoSlots),
                        buildNode(branch.consequence(), model, environment, deferredChecksByNode, foldedReads, memoSlots)))
                .toList();
        ExecutableNode elseExpression =
                buildNode(conditional.elseExpression(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
        return foldConditional(new ConditionalExecutableNode(conditional.id(), conditional.sourceSpan(), branches, elseExpression));
    }

    private ExecutableNode buildFunctionCall(
            FunctionCallNode functionCall,
            SemanticModel model,
            ExpressionEnvironment environment,
            Map<NodeId, List<DeferredCheck>> deferredChecksByNode,
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        FunctionDescriptor descriptor = BindingLookup.required(model.functionBindings(), functionCall.id(), "function binding");
        List<ExecutableNode> arguments = functionCall.arguments().stream()
                .map(ExpressionCallArgument.class::cast)
                .map(argument -> buildNode(argument.expression(), model, environment, deferredChecksByNode, foldedReads, memoSlots))
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
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        ExecutableNode current = buildNode(navigation.receiver(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
        for (NavigationLink link : navigation.links()) {
            current = buildNavigationLink(link, current, model, environment, deferredChecksByNode, foldedReads, memoSlots);
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
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        NavigationBinding binding = BindingLookup.required(model.navigationBindings(), link.id(), "navigation binding");
        NodeId id = link.id();
        var span = link.sourceSpan();
        return switch (binding) {
            case IndexSubscriptNavigationBinding indexBinding -> {
                IndexSubscriptNavigationLink index = (IndexSubscriptNavigationLink) link;
                boolean safe = indexBinding.safe();
                ExecutableNode built = new IndexSubscriptExecutableNode(id, span, receiver, index.index().value(), safe);
                yield foldNavigationLink(indexBinding.pure(), built, receiver);
            }
            case SliceSubscriptNavigationBinding sliceBinding -> {
                SliceSubscriptNavigationLink slice = (SliceSubscriptNavigationLink) link;
                boolean safe = sliceBinding.safe();
                ExecutableNode built = new SliceSubscriptExecutableNode(
                        id, span, receiver, SubscriptBounds.rawValue(slice.start()),
                        SubscriptBounds.rawValue(slice.end()), safe, environment.maxMaterializedSize());
                yield foldNavigationLink(sliceBinding.pure(), built, receiver);
            }
            case MapKeySubscriptNavigationBinding mapKeyBinding -> {
                StringKeySubscriptNavigationLink stringKey = (StringKeySubscriptNavigationLink) link;
                boolean safe = mapKeyBinding.safe();
                ExecutableNode built = new MapKeySubscriptExecutableNode(id, span, receiver, stringKey.key(), safe);
                yield foldNavigationLink(mapKeyBinding.pure(), built, receiver);
            }
            case FilterNavigationBinding filterBinding -> {
                FilterNavigationLink filter = (FilterNavigationLink) link;
                ExecutableNode predicate = buildNode(filter.predicate(), model, environment, deferredChecksByNode, foldedReads, memoSlots);
                yield new FilterExecutableNode(
                        id, span, receiver, filter.safe(), predicate, filterBinding.currentItemFrameSlot(),
                        environment.maxMaterializedSize());
            }
            case ContextualMemberNavigationBinding memberBinding -> {
                boolean safe = memberBinding.safe();
                ExecutableNode built = new ContextualMemberExecutableNode(id, span, receiver, memberBinding.member(), safe);
                yield foldNavigationLink(memberBinding.pure(), built, receiver);
            }
            case RegisteredPropertyNavigationBinding propertyBinding -> {
                boolean safe = propertyBinding.safe();
                ExecutableNode built = new RegisteredPropertyExecutableNode(id, span, receiver, safe, propertyBinding);
                yield foldNavigationLink(propertyBinding.pure(), built, receiver);
            }
            case RegisteredMethodNavigationBinding methodBinding -> {
                CallNavigationLink call = (CallNavigationLink) link;
                boolean safe = methodBinding.safe();
                List<ExecutableNode> arguments = call.arguments().stream()
                        .map(ExpressionCallArgument.class::cast)
                        .map(argument -> buildNode(argument.expression(), model, environment, deferredChecksByNode, foldedReads, memoSlots))
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
                        buildOperationArguments(call, operationBinding, model, environment, deferredChecksByNode, foldedReads, memoSlots);
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
            List<FoldedRead> foldedReads,
            Map<NodeId, Integer> memoSlots) {
        ArrayList<ExecutableNode> valueArguments = new ArrayList<>();
        ArrayList<ExecutableLambda> lambdaArguments = new ArrayList<>();
        int lambdaIndex = 0;
        for (CallArgument argument : call.arguments()) {
            if (argument instanceof ExpressionCallArgument expressionArgument) {
                valueArguments.add(buildNode(expressionArgument.expression(), model, environment, deferredChecksByNode, foldedReads, memoSlots));
                continue;
            }
            LambdaCallArgument lambdaArgument = (LambdaCallArgument) argument;
            CollectionOperationBinding.LambdaBinding lambdaBinding = binding.lambdaBindings().get(lambdaIndex++);
            LambdaNode lambda = lambdaArgument.lambda();
            lambdaArguments.add(new ExecutableLambda(
                    buildNode(lambda.body(), model, environment, deferredChecksByNode, foldedReads, memoSlots),
                    lambdaBinding.currentItemFrameSlot()));
        }
        return new ExecutableOperationArguments(valueArguments, lambdaArguments);
    }

    /**
     * Wraps {@code built} in a {@link MemoizedExecutableNode} when {@code nodeId} is one occurrence of
     * an eligible Subexpressao Comum Memoizada (ADR 0019, issue #121): {@code memoSlots} is empty in
     * Oracle mode, so this is a no-op there. A node that already folded to a {@link ConstantExecutableNode},
     * or that is already a single frame slot load ({@link FrameReadExecutableNode}, e.g. a repeated
     * identifier or Item Atual read), is never wrapped: both are already the cheapest possible read, and
     * a memo slot would only add a frame read and a branch on top for no benefit.
     */
    private ExecutableNode memoize(NodeId nodeId, ExecutableNode built, Map<NodeId, Integer> memoSlots) {
        Integer slot = memoSlots.get(nodeId);
        if (slot == null || built instanceof ConstantExecutableNode || built instanceof FrameReadExecutableNode) {
            return built;
        }
        return new MemoizedExecutableNode(nodeId, built.sourceSpan(), slot, built);
    }

    /**
     * Attempts eager constant folding (ADR 0019, issue #115) of an eager, non-lazy construct whose
     * required children are named explicitly, since {@code ExecutableNode} has no generic child
     * traversal. In oracle mode this is a no-op: {@code built} is always returned unchanged.
     */
    private ExecutableNode fold(ExecutableNode built, ExecutableNode... requiredConstantChildren) {
        return optimizing ? ConstantFolder.fold(built, requiredConstantChildren) : built;
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
        return optimizing ? ConstantFolder.foldNullCoalesce(built) : built;
    }

    /**
     * Attempts the structural fold of the conditional (ADR 0019, issue #116). In oracle mode this is a
     * no-op: {@code built} is always returned unchanged.
     */
    private ExecutableNode foldConditional(ConditionalExecutableNode built) {
        return optimizing ? ConstantFolder.foldConditional(built) : built;
    }

    /**
     * Attempts the scalar assertion elision (ADR 0019, issue #118). In oracle mode this is a no-op:
     * {@code built} is always returned unchanged.
     */
    private ExecutableNode foldAssertion(FunctionCallExecutableNode built) {
        return optimizing ? ConstantFolder.foldAssertion(built) : built;
    }

    /**
     * Attempts the double-negation elision (ADR 0019, issue #118). In oracle mode this is a no-op:
     * {@code built} is always returned unchanged.
     */
    private ExecutableNode foldDoubleNegation(UnaryExecutableNode built) {
        return optimizing ? ConstantFolder.foldDoubleNegation(built) : built;
    }

    /**
     * Attempts the membership download to a sorted array or hash set (ADR 0019, issue #119). In oracle
     * mode this is a no-op: {@code built} is always returned unchanged.
     */
    private ExecutableNode foldMembership(MembershipExecutableNode built) {
        return optimizing ? ConstantFolder.foldMembership(built) : built;
    }

    private static ExpressionEnvironment requireEnvironment(ExpressionEnvironment environment) {
        return Objects.requireNonNull(environment, "environment");
    }

}
