package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.internal.ast.BetweenNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.CallArgument;
import com.runestone.expeval_mk3.internal.ast.CallNavigationLink;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalBranchNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalNode;
import com.runestone.expeval_mk3.internal.ast.CurrentItemNode;
import com.runestone.expeval_mk3.internal.ast.CurrentTemporalValueNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionCallArgument;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.FilterNavigationLink;
import com.runestone.expeval_mk3.internal.ast.FunctionCallNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.IndexSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.LambdaCallArgument;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.MembershipNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperatorOccurrence;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.StringKeySubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SubscriptBounds;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.runtime.ConstantFolder;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationBinding;
import com.runestone.expeval_mk3.internal.semantics.ContextualMemberNavigationBinding;
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
import java.util.regex.Pattern;

/**
 * Finds Subexpressao Comum Memoizada candidates (issue #121, ADR 0019) by walking the same AST shape
 * {@code ExecutionPlanBuilder} builds, once, ahead of construction: it groups every subtree by a
 * {@link StructuralKey} that ignores {@code NodeId} and source span, and assigns one frame slot, past
 * the semantic {@code frameSize}, to every key that occurs at least twice, is pure, and never reads an
 * Item Atual slot or an internal (assignable) symbol slot. Internal symbols are excluded even though
 * {@code SemanticModel} purity says nothing about them: two occurrences can read the same slot at
 * different values when an intervening assignment reassigns it, and reassignment reuses the frame slot
 * (see {@code ExecutionPlanBuilder#buildAssignedSymbolsInCreationOrder}), so purity alone would let
 * memoization collapse two genuinely different values into one.
 */
final class CommonSubexpressionAnalyzer {

    private final SemanticModel model;
    private final Map<StructuralKey, Occurrence> occurrencesByKey = new LinkedHashMap<>();

    private CommonSubexpressionAnalyzer(SemanticModel model) {
        this.model = model;
    }

    static CommonSubexpressionAnalysis analyze(SemanticModel model, CalculationPointInventory calculationPoints) {
        CommonSubexpressionAnalyzer analyzer = new CommonSubexpressionAnalyzer(model);
        model.ast().resultExpression().ifPresent(analyzer::analyzeExpression);
        model.ast().assignments().forEach(assignment -> analyzer.analyzeExpression(assignment.expression()));
        return analyzer.buildAnalysis(calculationPoints);
    }

    static CommonSubexpressionAnalysis analyze(SemanticModel model) {
        return analyze(model, CalculationPointInventory.build(model));
    }

    private CommonSubexpressionAnalysis buildAnalysis(CalculationPointInventory calculationPoints) {
        int frameSize = model.frameLayout().frameSize();
        int nextSlot = frameSize;
        Map<NodeId, Integer> memoSlotsByNodeId = new LinkedHashMap<>();
        List<Occurrence> memoizedGroups = new ArrayList<>();
        for (Occurrence occurrence : occurrencesByKey.values()) {
            List<NodeId> nodeIds = occurrence.nodeIds();
            NodeAnalysis analysis = occurrence.analyses().getFirst();
            if (nodeIds.size() < 2
                    || analysis.containsCurrentItem()
                    || analysis.containsInternalSymbol()
                    || !model.purityOf(nodeIds.getFirst())) {
                continue;
            }
            int slot = nextSlot++;
            nodeIds.forEach(nodeId -> memoSlotsByNodeId.put(nodeId, slot));
            memoizedGroups.add(occurrence);
        }
        int memoSlotCount = nextSlot - frameSize;
        Map<NodeId, List<Integer>> replaySlotsByCalculationNode = new LinkedHashMap<>();
        List<int[]> replaySlotsByMemoizedGroup = new ArrayList<>();
        for (Occurrence group : memoizedGroups) {
            List<CalculationPoint> firstPoints = group.analyses().getFirst().calculationPoints();
            int[] groupReplaySlots = new int[firstPoints.size()];
            for (int pointIndex = 0; pointIndex < firstPoints.size(); pointIndex++) {
                groupReplaySlots[pointIndex] = nextSlot++;
            }
            replaySlotsByMemoizedGroup.add(groupReplaySlots);
            for (int occurrenceIndex = 0; occurrenceIndex < group.analyses().size(); occurrenceIndex++) {
                List<CalculationPoint> occurrencePoints = group.analyses().get(occurrenceIndex).calculationPoints();
                if (occurrencePoints.size() != firstPoints.size()) {
                    throw new IllegalStateException("structurally equivalent CSE occurrences have different capture shapes");
                }
                for (int pointIndex = 0; pointIndex < firstPoints.size(); pointIndex++) {
                    replaySlotsByCalculationNode
                            .computeIfAbsent(occurrencePoints.get(pointIndex).nodeId(), ignored -> new ArrayList<>())
                            .add(groupReplaySlots[pointIndex]);
                }
            }
        }
        Map<NodeId, int[]> replaySlotsByCalculationNodeId = new LinkedHashMap<>();
        replaySlotsByCalculationNode.forEach(
                (nodeId, slots) -> replaySlotsByCalculationNodeId.put(nodeId, toIntArray(slots)));
        Map<NodeId, MemoizedOccurrence> memoizedOccurrencesByNodeId = new LinkedHashMap<>();
        for (int groupIndex = 0; groupIndex < memoizedGroups.size(); groupIndex++) {
            Occurrence group = memoizedGroups.get(groupIndex);
            int[] groupReplaySlots = replaySlotsByMemoizedGroup.get(groupIndex);
            for (int occurrenceIndex = 0; occurrenceIndex < group.nodeIds().size(); occurrenceIndex++) {
                NodeAnalysis analysis = group.analyses().get(occurrenceIndex);
                List<Integer> publicSlots = new ArrayList<>();
                List<Integer> replaySlots = new ArrayList<>();
                for (int pointIndex = 0; pointIndex < analysis.calculationPoints().size(); pointIndex++) {
                    CalculationPoint point = analysis.calculationPoints().get(pointIndex);
                    int publicSlot = calculationPoints.slot(point.nodeId());
                    if (publicSlot >= 0) {
                        publicSlots.add(publicSlot);
                        replaySlots.add(groupReplaySlots[pointIndex]);
                    }
                }
                NodeId nodeId = group.nodeIds().get(occurrenceIndex);
                memoizedOccurrencesByNodeId.put(nodeId,
                        new MemoizedOccurrence(
                                memoSlotsByNodeId.get(nodeId), toIntArray(publicSlots), toIntArray(replaySlots)));
            }
        }
        return new CommonSubexpressionAnalysis(
                memoSlotsByNodeId,
                memoizedOccurrencesByNodeId,
                replaySlotsByCalculationNodeId,
                memoSlotCount,
                nextSlot - frameSize - memoSlotCount);
    }

    private record NodeAnalysis(
            StructuralKey key,
            boolean containsCurrentItem,
            boolean containsInternalSymbol,
            List<CalculationPoint> calculationPoints) {
    }

    private record CalculationPoint(NodeId nodeId) {
    }

    /** All occurrences recorded so far under one {@link StructuralKey}, alongside their shared analysis. */
    private record Occurrence(List<NodeId> nodeIds, List<NodeAnalysis> analyses) {
    }

    private NodeAnalysis analyzeExpression(ExpressionNode node) {
        if (node instanceof GroupedExpressionNode grouped) {
            return analyzeExpression(grouped.expression());
        }
        NodeAnalysis analysis = computeAnalysis(node);
        if (isMemoWrapCandidate(node)) {
            record(node.id(), analysis);
        }
        return analysis;
    }

    /**
     * Excludes node kinds that {@code ExecutionPlanBuilder} never builds as anything other than a
     * {@code ConstantExecutableNode} or a {@code FrameReadExecutableNode} (a literal, a plain identifier
     * read, or a current-item read): both are already a single value or a single array load, so a memo
     * slot for either would only add a frame read and a branch on top, never a benefit. Recording them
     * anyway would still cost a real frame slot even though {@code ExecutionPlanBuilder#memoize} always
     * declines to wrap them.
     */
    private static boolean isMemoWrapCandidate(ExpressionNode node) {
        return !(node instanceof LiteralNode) && !(node instanceof IdentifierNode) && !(node instanceof CurrentItemNode);
    }

    private NodeAnalysis computeAnalysis(ExpressionNode node) {
        return switch (node) {
            case GroupedExpressionNode grouped ->
                    throw new IllegalStateException("grouped expressions are unwrapped before dispatch");
            case LiteralNode literal -> new NodeAnalysis(
                    StructuralKey.of("Literal", model.preparedValues().get(literal.id())), false, false, List.of());
            case IdentifierNode identifier -> analyzeIdentifier(identifier);
            case CurrentItemNode currentItem -> analyzeCurrentItem(currentItem);
            case CurrentTemporalValueNode currentTemporalValue -> calculationPoint(
                    currentTemporalValue.id(), StructuralKey.of("CurrentTemporal", currentTemporalValue.kind()), false, false);
            case CollectionLiteralNode collection -> analyzeCollectionLiteral(collection);
            case BinaryOperationNode binary -> analyzeBinary(binary);
            case UnaryOperationNode unary -> analyzeUnary(unary);
            case PostfixOperationNode postfix -> analyzePostfix(postfix);
            case BetweenNode between -> analyzeBetween(between);
            case MembershipNode membership -> analyzeMembership(membership);
            case NullCoalesceNode coalesce -> analyzeNullCoalesce(coalesce);
            case ConditionalNode conditional -> analyzeConditional(conditional);
            case FunctionCallNode functionCall -> analyzeFunctionCall(functionCall);
            case NavigationChainNode navigation -> analyzeNavigation(navigation);
        };
    }

    private NodeAnalysis analyzeIdentifier(IdentifierNode identifier) {
        SymbolBinding binding = BindingLookup.required(model.symbolBindings(), identifier.id(), "symbol binding");
        StructuralKey key = StructuralKey.of("Identifier", binding.frameSlot());
        return new NodeAnalysis(key, false, !binding.external(), List.of());
    }

    private NodeAnalysis analyzeCurrentItem(CurrentItemNode currentItem) {
        SymbolBinding binding = BindingLookup.required(model.symbolBindings(), currentItem.id(), "current item binding");
        StructuralKey key = StructuralKey.of("CurrentItem", binding.frameSlot());
        return new NodeAnalysis(key, true, false, List.of());
    }

    private NodeAnalysis analyzeCollectionLiteral(CollectionLiteralNode collection) {
        List<NodeAnalysis> elements = collection.elements().stream().map(this::analyzeExpression).toList();
        StructuralKey key = StructuralKey.of("CollectionLiteral", keysOf(elements));
        return new NodeAnalysis(key, anyCurrentItem(elements), anyInternalSymbol(elements), pointsOf(elements));
    }

    private NodeAnalysis analyzeBinary(BinaryOperationNode binary) {
        NodeAnalysis left = analyzeExpression(binary.left());
        BinaryOperator operator = binary.operator();
        if (operator == BinaryOperator.REGEX_MATCH || operator == BinaryOperator.REGEX_NOT_MATCH) {
            Pattern pattern = (Pattern) model.preparedValues().get(binary.id());
            StructuralKey key = StructuralKey.of("Binary:" + operator, left.key(), new IdentityKey(pattern));
            return new NodeAnalysis(key, left.containsCurrentItem(), left.containsInternalSymbol(), left.calculationPoints());
        }
        NodeAnalysis right = analyzeExpression(binary.right());
        StructuralKey key = StructuralKey.of("Binary:" + operator, left.key(), right.key());
        return new NodeAnalysis(
                key,
                left.containsCurrentItem() || right.containsCurrentItem(),
                left.containsInternalSymbol() || right.containsInternalSymbol(),
                pointsOf(left, right));
    }

    private NodeAnalysis analyzeUnary(UnaryOperationNode unary) {
        NodeAnalysis operand = analyzeExpression(unary.operand());
        StructuralKey key = StructuralKey.of("Unary:" + unary.operator(), operand.key());
        return new NodeAnalysis(key, operand.containsCurrentItem(), operand.containsInternalSymbol(), operand.calculationPoints());
    }

    private NodeAnalysis analyzePostfix(PostfixOperationNode postfix) {
        NodeAnalysis operand = analyzeExpression(postfix.operand());
        List<?> operators = postfix.operations().stream().map(PostfixOperatorOccurrence::operator).toList();
        StructuralKey key = StructuralKey.of("Postfix", operand.key(), operators);
        return new NodeAnalysis(key, operand.containsCurrentItem(), operand.containsInternalSymbol(), operand.calculationPoints());
    }

    private NodeAnalysis analyzeBetween(BetweenNode between) {
        NodeAnalysis value = analyzeExpression(between.value());
        NodeAnalysis lower = analyzeExpression(between.lowerBound());
        NodeAnalysis upper = analyzeExpression(between.upperBound());
        StructuralKey key = StructuralKey.of("Between", between.negated(), value.key(), lower.key(), upper.key());
        return new NodeAnalysis(
                key,
                value.containsCurrentItem() || lower.containsCurrentItem() || upper.containsCurrentItem(),
                value.containsInternalSymbol() || lower.containsInternalSymbol() || upper.containsInternalSymbol(),
                pointsOf(value, lower, upper));
    }

    private NodeAnalysis analyzeMembership(MembershipNode membership) {
        NodeAnalysis element = analyzeExpression(membership.element());
        NodeAnalysis collection = analyzeExpression(membership.collection());
        StructuralKey key = StructuralKey.of("Membership", membership.negated(), element.key(), collection.key());
        return new NodeAnalysis(
                key,
                element.containsCurrentItem() || collection.containsCurrentItem(),
                element.containsInternalSymbol() || collection.containsInternalSymbol(),
                pointsOf(element, collection));
    }

    private NodeAnalysis analyzeNullCoalesce(NullCoalesceNode coalesce) {
        List<NodeAnalysis> operands = coalesce.operands().stream().map(this::analyzeExpression).toList();
        StructuralKey key = StructuralKey.of("NullCoalesce", keysOf(operands));
        return new NodeAnalysis(key, anyCurrentItem(operands), anyInternalSymbol(operands), pointsOf(operands));
    }

    private NodeAnalysis analyzeConditional(ConditionalNode conditional) {
        List<Object> branchKeys = new ArrayList<>();
        boolean containsCurrentItem = false;
        boolean containsInternalSymbol = false;
        List<CalculationPoint> calculationPoints = new ArrayList<>();
        for (ConditionalBranchNode branch : conditional.branches()) {
            NodeAnalysis condition = analyzeExpression(branch.condition());
            NodeAnalysis consequence = analyzeExpression(branch.consequence());
            branchKeys.add(List.of(condition.key(), consequence.key()));
            containsCurrentItem |= condition.containsCurrentItem() || consequence.containsCurrentItem();
            containsInternalSymbol |= condition.containsInternalSymbol() || consequence.containsInternalSymbol();
            calculationPoints.addAll(condition.calculationPoints());
            calculationPoints.addAll(consequence.calculationPoints());
        }
        NodeAnalysis elseAnalysis = analyzeExpression(conditional.elseExpression());
        containsCurrentItem |= elseAnalysis.containsCurrentItem();
        containsInternalSymbol |= elseAnalysis.containsInternalSymbol();
        calculationPoints.addAll(elseAnalysis.calculationPoints());
        StructuralKey key = StructuralKey.of("Conditional", branchKeys, elseAnalysis.key());
        return new NodeAnalysis(key, containsCurrentItem, containsInternalSymbol, List.copyOf(calculationPoints));
    }

    private NodeAnalysis analyzeFunctionCall(FunctionCallNode functionCall) {
        FunctionDescriptor descriptor = BindingLookup.required(model.functionBindings(), functionCall.id(), "function binding");
        List<NodeAnalysis> arguments = functionCall.arguments().stream()
                .map(ExpressionCallArgument.class::cast)
                .map(argument -> analyzeExpression(argument.expression()))
                .toList();
        StructuralKey key = StructuralKey.of("FunctionCall", new IdentityKey(descriptor), keysOf(arguments));
        List<CalculationPoint> points = new ArrayList<>(pointsOf(arguments));
        if (!ConstantFolder.isElidableAssertion(descriptor)) {
            points.add(new CalculationPoint(functionCall.id()));
        }
        return new NodeAnalysis(key, anyCurrentItem(arguments), anyInternalSymbol(arguments), List.copyOf(points));
    }

    private NodeAnalysis analyzeNavigation(NavigationChainNode navigation) {
        NodeAnalysis current = analyzeExpression(navigation.receiver());
        for (NavigationLink link : navigation.links()) {
            current = analyzeNavigationLink(link, current);
        }
        return current;
    }

    /**
     * Filter, wildcard, and collection-operation links are treated as an Item Atual boundary: their
     * predicate or lambda body reads a current-item slot by construction (issue #121 excludes any
     * subtree that reads one), so the chain up to and including this link is never itself eligible.
     * Their inner expressions are still analyzed, so a repeated pure subexpression inside a predicate
     * or lambda body remains its own, independently eligible, candidate.
     */
    private NodeAnalysis analyzeNavigationLink(NavigationLink link, NodeAnalysis receiver) {
        NavigationBinding binding = BindingLookup.required(model.navigationBindings(), link.id(), "navigation binding");
        return switch (binding) {
            case IndexSubscriptNavigationBinding indexBinding -> {
                IndexSubscriptNavigationLink index = (IndexSubscriptNavigationLink) link;
                StructuralKey key = StructuralKey.of(
                        "IndexSubscript", receiver.key(), index.index().value(), indexBinding.safe());
                yield new NodeAnalysis(key, receiver.containsCurrentItem(), receiver.containsInternalSymbol(), receiver.calculationPoints());
            }
            case SliceSubscriptNavigationBinding sliceBinding -> {
                SliceSubscriptNavigationLink slice = (SliceSubscriptNavigationLink) link;
                StructuralKey key = StructuralKey.of(
                        "SliceSubscript", receiver.key(),
                        SubscriptBounds.rawValue(slice.start()), SubscriptBounds.rawValue(slice.end()),
                        sliceBinding.safe());
                yield new NodeAnalysis(key, receiver.containsCurrentItem(), receiver.containsInternalSymbol(), receiver.calculationPoints());
            }
            case MapKeySubscriptNavigationBinding mapKeyBinding -> {
                StringKeySubscriptNavigationLink stringKey = (StringKeySubscriptNavigationLink) link;
                StructuralKey key =
                        StructuralKey.of("MapKeySubscript", receiver.key(), stringKey.key(), mapKeyBinding.safe());
                yield new NodeAnalysis(key, receiver.containsCurrentItem(), receiver.containsInternalSymbol(), receiver.calculationPoints());
            }
            case ContextualMemberNavigationBinding memberBinding -> {
                StructuralKey key = StructuralKey.of(
                        "ContextualMember", receiver.key(), memberBinding.member(), memberBinding.safe());
                yield new NodeAnalysis(key, receiver.containsCurrentItem(), receiver.containsInternalSymbol(), receiver.calculationPoints());
            }
            case RegisteredPropertyNavigationBinding propertyBinding -> {
                StructuralKey key = StructuralKey.of(
                        "RegisteredProperty", receiver.key(), new IdentityKey(propertyBinding.accessorHandle()),
                        propertyBinding.safe());
                yield appendCalculationPoint(receiver, link.id(), key);
            }
            case RegisteredMethodNavigationBinding methodBinding -> {
                CallNavigationLink call = (CallNavigationLink) link;
                List<NodeAnalysis> arguments = call.arguments().stream()
                        .map(ExpressionCallArgument.class::cast)
                        .map(argument -> analyzeExpression(argument.expression()))
                        .toList();
                StructuralKey key = StructuralKey.of(
                        "RegisteredMethod", receiver.key(), new IdentityKey(methodBinding.invocationHandle()),
                        methodBinding.safe(), keysOf(arguments));
                yield new NodeAnalysis(
                        key,
                        receiver.containsCurrentItem() || anyCurrentItem(arguments),
                        receiver.containsInternalSymbol() || anyInternalSymbol(arguments),
                        appendPoint(pointsOf(receiver, arguments), new CalculationPoint(link.id())));
            }
            case FilterNavigationBinding ignored -> {
                FilterNavigationLink filter = (FilterNavigationLink) link;
                analyzeExpression(filter.predicate());
                yield new NodeAnalysis(
                        StructuralKey.of("Filter", link.id()), true, receiver.containsInternalSymbol(), receiver.calculationPoints());
            }
            case WildcardNavigationBinding ignored -> new NodeAnalysis(
                    StructuralKey.of("Wildcard", link.id()), true, receiver.containsInternalSymbol(), receiver.calculationPoints());
            case CollectionOperationBinding operationBinding -> {
                CallNavigationLink call = (CallNavigationLink) link;
                boolean internalSymbol = receiver.containsInternalSymbol();
                for (CallArgument argument : call.arguments()) {
                    if (argument instanceof ExpressionCallArgument expressionArgument) {
                        internalSymbol |= analyzeExpression(expressionArgument.expression()).containsInternalSymbol();
                    } else if (argument instanceof LambdaCallArgument lambdaArgument) {
                        internalSymbol |= analyzeExpression(lambdaArgument.lambda().body()).containsInternalSymbol();
                    }
                }
                yield new NodeAnalysis(
                        StructuralKey.of("CollectionOperation", link.id()), true, internalSymbol, receiver.calculationPoints());
            }
        };
    }

    private void record(NodeId nodeId, NodeAnalysis analysis) {
        Occurrence occurrence = occurrencesByKey.computeIfAbsent(
                analysis.key(), ignored -> new Occurrence(new ArrayList<>(), new ArrayList<>()));
        occurrence.nodeIds().add(nodeId);
        occurrence.analyses().add(analysis);
    }

    private static NodeAnalysis calculationPoint(
            NodeId nodeId, StructuralKey key, boolean containsCurrentItem, boolean containsInternalSymbol) {
        return new NodeAnalysis(
                key, containsCurrentItem, containsInternalSymbol, List.of(new CalculationPoint(nodeId)));
    }

    private static NodeAnalysis appendCalculationPoint(NodeAnalysis receiver, NodeId nodeId, StructuralKey key) {
        return new NodeAnalysis(
                key,
                receiver.containsCurrentItem(),
                receiver.containsInternalSymbol(),
                appendPoint(receiver.calculationPoints(), new CalculationPoint(nodeId)));
    }

    private static List<CalculationPoint> pointsOf(NodeAnalysis... analyses) {
        return pointsOf(List.of(analyses));
    }

    private static List<CalculationPoint> pointsOf(List<NodeAnalysis> analyses) {
        List<CalculationPoint> points = new ArrayList<>();
        analyses.forEach(analysis -> points.addAll(analysis.calculationPoints()));
        return List.copyOf(points);
    }

    private static List<CalculationPoint> pointsOf(NodeAnalysis receiver, List<NodeAnalysis> arguments) {
        List<CalculationPoint> points = new ArrayList<>(receiver.calculationPoints());
        arguments.forEach(argument -> points.addAll(argument.calculationPoints()));
        return List.copyOf(points);
    }

    private static List<CalculationPoint> appendPoint(List<CalculationPoint> points, CalculationPoint point) {
        List<CalculationPoint> appended = new ArrayList<>(points);
        appended.add(point);
        return List.copyOf(appended);
    }

    private static int[] toIntArray(List<Integer> slots) {
        return slots.stream().mapToInt(Integer::intValue).toArray();
    }

    private static List<StructuralKey> keysOf(List<NodeAnalysis> analyses) {
        return analyses.stream().map(NodeAnalysis::key).toList();
    }

    private static boolean anyCurrentItem(List<NodeAnalysis> analyses) {
        return analyses.stream().anyMatch(NodeAnalysis::containsCurrentItem);
    }

    private static boolean anyInternalSymbol(List<NodeAnalysis> analyses) {
        return analyses.stream().anyMatch(NodeAnalysis::containsInternalSymbol);
    }
}
