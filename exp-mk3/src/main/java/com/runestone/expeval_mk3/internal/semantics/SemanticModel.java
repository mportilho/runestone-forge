package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.AssignmentNode;
import com.runestone.expeval_mk3.internal.ast.AstNode;
import com.runestone.expeval_mk3.internal.ast.BetweenNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.CallNavigationLink;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalBranchNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalNode;
import com.runestone.expeval_mk3.internal.ast.CurrentItemNode;
import com.runestone.expeval_mk3.internal.ast.CurrentTemporalValueNode;
import com.runestone.expeval_mk3.internal.ast.DestructuringAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionCallArgument;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.FilterNavigationLink;
import com.runestone.expeval_mk3.internal.ast.FunctionCallNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierAssignmentTargetNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.IndexSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.LambdaCallArgument;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.MembershipNode;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.PropertyNavigationLink;
import com.runestone.expeval_mk3.internal.ast.SliceSubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.StringKeySubscriptNavigationLink;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.WildcardNavigationLink;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class SemanticModel {

    private final ExpressionFileNode ast;
    private final Map<NodeId, ExpressionType> resolvedTypes;
    private final Map<NodeId, RuntimeNullability> runtimeNullability;
    private final Map<NodeId, Object> preparedValues;
    private final Map<NodeId, CollectionShape> collectionShapes;
    private final Map<NodeId, Boolean> expressionPurity;
    private final Map<NodeId, SymbolBinding> symbolBindings;
    private final Map<NodeId, ExpressionType> equalityOperandTypes;
    private final Map<NodeId, FunctionDescriptor> functionBindings;
    private final Map<NodeId, NavigationBinding> navigationBindings;
    private final Map<NodeId, NumericFact> numericFacts;
    private final List<DeferredCheck> deferredChecks;
    private final FrameLayout frameLayout;

    SemanticModel(
            ExpressionFileNode ast,
            Map<NodeId, ExpressionType> resolvedTypes,
            Map<NodeId, RuntimeNullability> runtimeNullability,
            Map<NodeId, Object> preparedValues,
            Map<NodeId, CollectionShape> collectionShapes,
            Map<NodeId, Boolean> expressionPurity,
            Map<NodeId, SymbolBinding> symbolBindings,
            Map<NodeId, ExpressionType> equalityOperandTypes,
            Map<NodeId, FunctionDescriptor> functionBindings,
            Map<NodeId, NavigationBinding> navigationBindings,
            Map<NodeId, NumericFact> numericFacts,
            List<DeferredCheck> deferredChecks,
            FrameLayout frameLayout) {
        this.ast = Objects.requireNonNull(ast, "ast");
        this.resolvedTypes = Map.copyOf(resolvedTypes);
        this.runtimeNullability = Map.copyOf(runtimeNullability);
        this.preparedValues = Map.copyOf(preparedValues);
        this.collectionShapes = Map.copyOf(collectionShapes);
        this.expressionPurity = Map.copyOf(expressionPurity);
        this.symbolBindings = Map.copyOf(symbolBindings);
        this.equalityOperandTypes = Map.copyOf(equalityOperandTypes);
        this.functionBindings = Map.copyOf(functionBindings);
        this.navigationBindings = Map.copyOf(navigationBindings);
        this.numericFacts = Map.copyOf(numericFacts);
        this.deferredChecks = List.copyOf(deferredChecks);
        this.frameLayout = Objects.requireNonNull(frameLayout, "frameLayout");
        Set<NodeId> visitedNodeIds = new HashSet<>();
        ast.assignments().forEach(assignment -> validateAssignment(assignment, visitedNodeIds));
        ast.resultExpression().ifPresent(expression -> validateCompleteExpression(expression, visitedNodeIds));
        validateFrameLayout();
        validateDeferredChecks(visitedNodeIds);
    }

    public ExpressionFileNode ast() {
        return ast;
    }

    public Map<NodeId, ExpressionType> resolvedTypes() {
        return resolvedTypes;
    }

    public Map<NodeId, RuntimeNullability> runtimeNullability() {
        return runtimeNullability;
    }

    public Map<NodeId, Object> preparedValues() {
        return preparedValues;
    }

    public Map<NodeId, CollectionShape> collectionShapes() {
        return collectionShapes;
    }

    public Map<NodeId, Boolean> expressionPurity() {
        return expressionPurity;
    }

    public boolean purityOf(NodeId nodeId) {
        Boolean pure = expressionPurity.get(nodeId);
        if (pure == null) {
            throw new IllegalStateException("no purity recorded for node " + nodeId);
        }
        return pure;
    }

    public Map<NodeId, SymbolBinding> symbolBindings() {
        return symbolBindings;
    }

    public Map<NodeId, ExpressionType> equalityOperandTypes() {
        return equalityOperandTypes;
    }

    public Map<NodeId, FunctionDescriptor> functionBindings() {
        return functionBindings;
    }

    public Map<NodeId, NavigationBinding> navigationBindings() {
        return navigationBindings;
    }

    public NumericFact numericFactOf(NodeId nodeId) {
        NumericFact fact = numericFacts.get(nodeId);
        if (fact == null) {
            throw new IllegalStateException("no numeric fact recorded for node " + nodeId);
        }
        return fact;
    }

    public Map<NodeId, NumericFact> numericFacts() {
        return numericFacts;
    }

    public List<DeferredCheck> deferredChecks() {
        return deferredChecks;
    }

    public FrameLayout frameLayout() {
        return frameLayout;
    }

    private void validateAssignment(AssignmentNode assignment, Set<NodeId> visited) {
        validateCompleteExpression(assignment.expression(), visited);
        switch (assignment.target()) {
            case IdentifierAssignmentTargetNode target -> {
                visited.add(target.id());
                requireEntry(symbolBindings, target, "assignment target binding");
            }
            case DestructuringAssignmentTargetNode target -> {
                visited.add(target.id());
                target.elements().forEach(element -> {
                    visited.add(element.id());
                    requireEntry(symbolBindings, element, "assignment target binding");
                });
            }
        }
    }

    private void validateCompleteExpression(ExpressionNode expression, Set<NodeId> visited) {
        visited.add(expression.id());
        requireEntry(resolvedTypes, expression, "resolved type");
        requireEntry(runtimeNullability, expression, "runtime nullability");
        requireEntry(expressionPurity, expression, "purity");
        requireNumericFactIfNumeric(expression);
        switch (expression) {
            case LiteralNode literal -> requireEntry(preparedValues, literal, "prepared literal value");
            case IdentifierNode identifier -> requireEntry(symbolBindings, identifier, "symbol binding");
            case CurrentItemNode currentItem -> requireEntry(symbolBindings, currentItem, "current item binding");
            case CurrentTemporalValueNode ignored -> {
            }
            case CollectionLiteralNode collection -> {
                requireEntry(collectionShapes, collection, "collection shape");
                collection.elements().forEach(element -> validateCompleteExpression(element, visited));
            }
            case GroupedExpressionNode grouped -> validateCompleteExpression(grouped.expression(), visited);
            case BinaryOperationNode binary -> {
                if (binary.operator() == BinaryOperator.EQUAL || binary.operator() == BinaryOperator.NOT_EQUAL) {
                    requireEntry(equalityOperandTypes, binary, "equality binding");
                }
                validateCompleteExpression(binary.left(), visited);
                validateCompleteExpression(binary.right(), visited);
            }
            case UnaryOperationNode unary -> validateCompleteExpression(unary.operand(), visited);
            case PostfixOperationNode postfix -> validateCompleteExpression(postfix.operand(), visited);
            case BetweenNode between -> {
                validateCompleteExpression(between.value(), visited);
                validateCompleteExpression(between.lowerBound(), visited);
                validateCompleteExpression(between.upperBound(), visited);
            }
            case MembershipNode membership -> {
                validateCompleteExpression(membership.element(), visited);
                validateCompleteExpression(membership.collection(), visited);
            }
            case NullCoalesceNode coalesce ->
                    coalesce.operands().forEach(operand -> validateCompleteExpression(operand, visited));
            case ConditionalNode conditional -> {
                conditional.branches().forEach(branch -> validateConditionalBranch(branch, visited));
                validateCompleteExpression(conditional.elseExpression(), visited);
            }
            case FunctionCallNode functionCall -> {
                requireEntry(functionBindings, functionCall, "function binding");
                functionCall.arguments().forEach(argument -> {
                    if (argument instanceof ExpressionCallArgument expressionArgument) {
                        validateCompleteExpression(expressionArgument.expression(), visited);
                    }
                });
            }
            case NavigationChainNode navigation -> {
                validateCompleteExpression(navigation.receiver(), visited);
                navigation.links().forEach(link -> validateNavigationLink(link, visited));
            }
        }
    }

    private void validateNavigationLink(NavigationLink link, Set<NodeId> visited) {
        visited.add(link.id());
        requireEntry(resolvedTypes, link, "navigation link resolved type");
        requireEntry(runtimeNullability, link, "navigation link runtime nullability");
        requireEntry(expressionPurity, link, "navigation link purity");
        requireEntry(navigationBindings, link, "navigation binding");
        requireNumericFactIfNumeric(link);
        switch (link) {
            case FilterNavigationLink filter -> {
                requireEntry(symbolBindings, filter, "filter current item binding");
                validateCompleteExpression(filter.predicate(), visited);
            }
            case CallNavigationLink call -> call.arguments().forEach(argument -> {
                if (argument instanceof ExpressionCallArgument expressionArgument) {
                    validateCompleteExpression(expressionArgument.expression(), visited);
                } else if (argument instanceof LambdaCallArgument lambdaArgument) {
                    requireEntry(symbolBindings, lambdaArgument.lambda().currentItem(), "lambda current item binding");
                    visited.add(lambdaArgument.lambda().currentItem().id());
                    validateCompleteExpression(lambdaArgument.lambda().body(), visited);
                }
            });
            case IndexSubscriptNavigationLink ignored -> {
            }
            case PropertyNavigationLink ignored -> {
            }
            case SliceSubscriptNavigationLink ignored -> {
            }
            case StringKeySubscriptNavigationLink ignored -> {
            }
            case WildcardNavigationLink ignored -> {
            }
        }
    }

    private void validateConditionalBranch(ConditionalBranchNode branch, Set<NodeId> visited) {
        validateCompleteExpression(branch.condition(), visited);
        validateCompleteExpression(branch.consequence(), visited);
    }

    private void validateFrameLayout() {
        for (SymbolBinding binding : symbolBindings.values()) {
            if (binding.frameSlot() >= frameLayout.frameSize()) {
                throw new IllegalStateException(
                        "successful semantic model has symbol binding '" + binding.name()
                                + "' with frame slot " + binding.frameSlot()
                                + " outside canonical frame layout size " + frameLayout.frameSize());
            }
            // identity comparison: the resolver reuses the same SymbolBinding instance in both maps
            if (binding.external() && !frameLayout.externalBindings().contains(binding)) {
                throw new IllegalStateException(
                        "successful semantic model has external symbol binding '" + binding.name()
                                + "' missing from the canonical frame layout");
            }
        }
    }

    private void validateDeferredChecks(Set<NodeId> visited) {
        for (DeferredCheck check : deferredChecks) {
            if (!visited.contains(check.nodeId())) {
                throw new IllegalStateException(
                        "successful semantic model has a " + check.getClass().getSimpleName()
                                + " referencing unknown node " + check.nodeId());
            }
        }
    }

    private void requireNumericFactIfNumeric(AstNode node) {
        if (resolvedTypes.get(node.id()) == ScalarType.NUMBER) {
            requireEntry(numericFacts, node, "numeric fact");
        }
    }

    private static <V> void requireEntry(Map<NodeId, V> values, AstNode node, String description) {
        if (!values.containsKey(node.id())) {
            throw new IllegalStateException("successful semantic model is missing " + description + " for "
                    + node.getClass().getSimpleName() + " " + node.id() + " at " + node.sourceSpan());
        }
    }
}
