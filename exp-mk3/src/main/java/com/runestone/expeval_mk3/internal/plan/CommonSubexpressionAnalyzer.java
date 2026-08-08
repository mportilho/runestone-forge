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

    static CommonSubexpressionAnalysis analyze(SemanticModel model) {
        CommonSubexpressionAnalyzer analyzer = new CommonSubexpressionAnalyzer(model);
        model.ast().resultExpression().ifPresent(analyzer::analyzeExpression);
        model.ast().assignments().forEach(assignment -> analyzer.analyzeExpression(assignment.expression()));
        return analyzer.buildAnalysis();
    }

    private CommonSubexpressionAnalysis buildAnalysis() {
        int frameSize = model.frameLayout().frameSize();
        int nextSlot = frameSize;
        Map<NodeId, Integer> memoSlotsByNodeId = new LinkedHashMap<>();
        for (Occurrence occurrence : occurrencesByKey.values()) {
            List<NodeId> nodeIds = occurrence.nodeIds();
            NodeAnalysis analysis = occurrence.analysis();
            if (nodeIds.size() < 2
                    || analysis.containsCurrentItem()
                    || analysis.containsInternalSymbol()
                    || !model.purityOf(nodeIds.getFirst())) {
                continue;
            }
            int slot = nextSlot++;
            nodeIds.forEach(nodeId -> memoSlotsByNodeId.put(nodeId, slot));
        }
        return new CommonSubexpressionAnalysis(memoSlotsByNodeId, nextSlot - frameSize);
    }

    private record NodeAnalysis(StructuralKey key, boolean containsCurrentItem, boolean containsInternalSymbol) {
    }

    /** All occurrences recorded so far under one {@link StructuralKey}, alongside their shared analysis. */
    private record Occurrence(NodeAnalysis analysis, List<NodeId> nodeIds) {
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
                    StructuralKey.of("Literal", model.preparedValues().get(literal.id())), false, false);
            case IdentifierNode identifier -> analyzeIdentifier(identifier);
            case CurrentItemNode currentItem -> analyzeCurrentItem(currentItem);
            case CurrentTemporalValueNode currentTemporalValue -> new NodeAnalysis(
                    StructuralKey.of("CurrentTemporal", currentTemporalValue.kind()), false, false);
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
        return new NodeAnalysis(key, false, !binding.external());
    }

    private NodeAnalysis analyzeCurrentItem(CurrentItemNode currentItem) {
        SymbolBinding binding = BindingLookup.required(model.symbolBindings(), currentItem.id(), "current item binding");
        StructuralKey key = StructuralKey.of("CurrentItem", binding.frameSlot());
        return new NodeAnalysis(key, true, false);
    }

    private NodeAnalysis analyzeCollectionLiteral(CollectionLiteralNode collection) {
        List<NodeAnalysis> elements = collection.elements().stream().map(this::analyzeExpression).toList();
        StructuralKey key = StructuralKey.of("CollectionLiteral", keysOf(elements));
        return new NodeAnalysis(key, anyCurrentItem(elements), anyInternalSymbol(elements));
    }

    private NodeAnalysis analyzeBinary(BinaryOperationNode binary) {
        NodeAnalysis left = analyzeExpression(binary.left());
        BinaryOperator operator = binary.operator();
        if (operator == BinaryOperator.REGEX_MATCH || operator == BinaryOperator.REGEX_NOT_MATCH) {
            Pattern pattern = (Pattern) model.preparedValues().get(binary.id());
            StructuralKey key = StructuralKey.of("Binary:" + operator, left.key(), new IdentityKey(pattern));
            return new NodeAnalysis(key, left.containsCurrentItem(), left.containsInternalSymbol());
        }
        NodeAnalysis right = analyzeExpression(binary.right());
        StructuralKey key = StructuralKey.of("Binary:" + operator, left.key(), right.key());
        return new NodeAnalysis(
                key,
                left.containsCurrentItem() || right.containsCurrentItem(),
                left.containsInternalSymbol() || right.containsInternalSymbol());
    }

    private NodeAnalysis analyzeUnary(UnaryOperationNode unary) {
        NodeAnalysis operand = analyzeExpression(unary.operand());
        StructuralKey key = StructuralKey.of("Unary:" + unary.operator(), operand.key());
        return new NodeAnalysis(key, operand.containsCurrentItem(), operand.containsInternalSymbol());
    }

    private NodeAnalysis analyzePostfix(PostfixOperationNode postfix) {
        NodeAnalysis operand = analyzeExpression(postfix.operand());
        List<?> operators = postfix.operations().stream().map(PostfixOperatorOccurrence::operator).toList();
        StructuralKey key = StructuralKey.of("Postfix", operand.key(), operators);
        return new NodeAnalysis(key, operand.containsCurrentItem(), operand.containsInternalSymbol());
    }

    private NodeAnalysis analyzeBetween(BetweenNode between) {
        NodeAnalysis value = analyzeExpression(between.value());
        NodeAnalysis lower = analyzeExpression(between.lowerBound());
        NodeAnalysis upper = analyzeExpression(between.upperBound());
        StructuralKey key = StructuralKey.of("Between", between.negated(), value.key(), lower.key(), upper.key());
        return new NodeAnalysis(
                key,
                value.containsCurrentItem() || lower.containsCurrentItem() || upper.containsCurrentItem(),
                value.containsInternalSymbol() || lower.containsInternalSymbol() || upper.containsInternalSymbol());
    }

    private NodeAnalysis analyzeMembership(MembershipNode membership) {
        NodeAnalysis element = analyzeExpression(membership.element());
        NodeAnalysis collection = analyzeExpression(membership.collection());
        StructuralKey key = StructuralKey.of("Membership", membership.negated(), element.key(), collection.key());
        return new NodeAnalysis(
                key,
                element.containsCurrentItem() || collection.containsCurrentItem(),
                element.containsInternalSymbol() || collection.containsInternalSymbol());
    }

    private NodeAnalysis analyzeNullCoalesce(NullCoalesceNode coalesce) {
        List<NodeAnalysis> operands = coalesce.operands().stream().map(this::analyzeExpression).toList();
        StructuralKey key = StructuralKey.of("NullCoalesce", keysOf(operands));
        return new NodeAnalysis(key, anyCurrentItem(operands), anyInternalSymbol(operands));
    }

    private NodeAnalysis analyzeConditional(ConditionalNode conditional) {
        List<Object> branchKeys = new ArrayList<>();
        boolean containsCurrentItem = false;
        boolean containsInternalSymbol = false;
        for (ConditionalBranchNode branch : conditional.branches()) {
            NodeAnalysis condition = analyzeExpression(branch.condition());
            NodeAnalysis consequence = analyzeExpression(branch.consequence());
            branchKeys.add(List.of(condition.key(), consequence.key()));
            containsCurrentItem |= condition.containsCurrentItem() || consequence.containsCurrentItem();
            containsInternalSymbol |= condition.containsInternalSymbol() || consequence.containsInternalSymbol();
        }
        NodeAnalysis elseAnalysis = analyzeExpression(conditional.elseExpression());
        containsCurrentItem |= elseAnalysis.containsCurrentItem();
        containsInternalSymbol |= elseAnalysis.containsInternalSymbol();
        StructuralKey key = StructuralKey.of("Conditional", branchKeys, elseAnalysis.key());
        return new NodeAnalysis(key, containsCurrentItem, containsInternalSymbol);
    }

    private NodeAnalysis analyzeFunctionCall(FunctionCallNode functionCall) {
        FunctionDescriptor descriptor = BindingLookup.required(model.functionBindings(), functionCall.id(), "function binding");
        List<NodeAnalysis> arguments = functionCall.arguments().stream()
                .map(ExpressionCallArgument.class::cast)
                .map(argument -> analyzeExpression(argument.expression()))
                .toList();
        StructuralKey key = StructuralKey.of("FunctionCall", new IdentityKey(descriptor), keysOf(arguments));
        return new NodeAnalysis(key, anyCurrentItem(arguments), anyInternalSymbol(arguments));
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
            case IndexSubscriptNavigationBinding ignored -> {
                IndexSubscriptNavigationLink index = (IndexSubscriptNavigationLink) link;
                StructuralKey key =
                        StructuralKey.of("IndexSubscript", receiver.key(), index.index().value(), index.safe());
                yield new NodeAnalysis(key, receiver.containsCurrentItem(), receiver.containsInternalSymbol());
            }
            case SliceSubscriptNavigationBinding ignored -> {
                SliceSubscriptNavigationLink slice = (SliceSubscriptNavigationLink) link;
                StructuralKey key = StructuralKey.of(
                        "SliceSubscript", receiver.key(),
                        SubscriptBounds.rawValue(slice.start()), SubscriptBounds.rawValue(slice.end()), slice.safe());
                yield new NodeAnalysis(key, receiver.containsCurrentItem(), receiver.containsInternalSymbol());
            }
            case MapKeySubscriptNavigationBinding ignored -> {
                StringKeySubscriptNavigationLink stringKey = (StringKeySubscriptNavigationLink) link;
                StructuralKey key =
                        StructuralKey.of("MapKeySubscript", receiver.key(), stringKey.key(), stringKey.safe());
                yield new NodeAnalysis(key, receiver.containsCurrentItem(), receiver.containsInternalSymbol());
            }
            case ContextualMemberNavigationBinding memberBinding -> {
                StructuralKey key =
                        StructuralKey.of("ContextualMember", receiver.key(), memberBinding.member(), link.safe());
                yield new NodeAnalysis(key, receiver.containsCurrentItem(), receiver.containsInternalSymbol());
            }
            case RegisteredPropertyNavigationBinding propertyBinding -> {
                StructuralKey key = StructuralKey.of(
                        "RegisteredProperty", receiver.key(), new IdentityKey(propertyBinding.accessorHandle()),
                        link.safe());
                yield new NodeAnalysis(key, receiver.containsCurrentItem(), receiver.containsInternalSymbol());
            }
            case RegisteredMethodNavigationBinding methodBinding -> {
                CallNavigationLink call = (CallNavigationLink) link;
                List<NodeAnalysis> arguments = call.arguments().stream()
                        .map(ExpressionCallArgument.class::cast)
                        .map(argument -> analyzeExpression(argument.expression()))
                        .toList();
                StructuralKey key = StructuralKey.of(
                        "RegisteredMethod", receiver.key(), new IdentityKey(methodBinding.invocationHandle()),
                        link.safe(), keysOf(arguments));
                yield new NodeAnalysis(
                        key,
                        receiver.containsCurrentItem() || anyCurrentItem(arguments),
                        receiver.containsInternalSymbol() || anyInternalSymbol(arguments));
            }
            case FilterNavigationBinding ignored -> {
                FilterNavigationLink filter = (FilterNavigationLink) link;
                analyzeExpression(filter.predicate());
                yield new NodeAnalysis(StructuralKey.of("Filter", link.id()), true, receiver.containsInternalSymbol());
            }
            case WildcardNavigationBinding ignored -> new NodeAnalysis(
                    StructuralKey.of("Wildcard", link.id()), true, receiver.containsInternalSymbol());
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
                yield new NodeAnalysis(StructuralKey.of("CollectionOperation", link.id()), true, internalSymbol);
            }
        };
    }

    private void record(NodeId nodeId, NodeAnalysis analysis) {
        occurrencesByKey.computeIfAbsent(analysis.key(), ignored -> new Occurrence(analysis, new ArrayList<>()))
                .nodeIds()
                .add(nodeId);
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
