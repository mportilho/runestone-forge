package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.internal.ast.AssignmentNode;
import com.runestone.expeval_mk3.internal.ast.BetweenNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.CallNavigationLink;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalBranchNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalNode;
import com.runestone.expeval_mk3.internal.ast.CurrentItemNode;
import com.runestone.expeval_mk3.internal.ast.CurrentTemporalValueNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.FilterNavigationLink;
import com.runestone.expeval_mk3.internal.ast.FunctionCallNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.LambdaCallArgument;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.MembershipNode;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;

import java.util.Map;
import java.util.Objects;

public final class SemanticModel {

    private final ExpressionFileNode ast;
    private final Map<NodeId, ExpressionType> resolvedTypes;
    private final Map<NodeId, RuntimeNullability> runtimeNullability;
    private final Map<NodeId, Object> preparedValues;
    private final Map<NodeId, CollectionShape> collectionShapes;
    private final Map<NodeId, SymbolBinding> symbolBindings;
    private final Map<NodeId, ExpressionType> equalityOperandTypes;
    private final Map<NodeId, FunctionDescriptor> functionBindings;
    private final Map<NodeId, CollectionOperationBinding> collectionOperationBindings;
    private final FrameLayout frameLayout;

    SemanticModel(
            ExpressionFileNode ast,
            Map<NodeId, ExpressionType> resolvedTypes,
            Map<NodeId, RuntimeNullability> runtimeNullability,
            Map<NodeId, Object> preparedValues,
            Map<NodeId, CollectionShape> collectionShapes,
            Map<NodeId, SymbolBinding> symbolBindings,
            Map<NodeId, ExpressionType> equalityOperandTypes,
            Map<NodeId, FunctionDescriptor> functionBindings,
            Map<NodeId, CollectionOperationBinding> collectionOperationBindings,
            FrameLayout frameLayout) {
        this.ast = Objects.requireNonNull(ast, "ast");
        this.resolvedTypes = Map.copyOf(resolvedTypes);
        this.runtimeNullability = Map.copyOf(runtimeNullability);
        this.preparedValues = Map.copyOf(preparedValues);
        this.collectionShapes = Map.copyOf(collectionShapes);
        this.symbolBindings = Map.copyOf(symbolBindings);
        this.equalityOperandTypes = Map.copyOf(equalityOperandTypes);
        this.functionBindings = Map.copyOf(functionBindings);
        this.collectionOperationBindings = Map.copyOf(collectionOperationBindings);
        this.frameLayout = Objects.requireNonNull(frameLayout, "frameLayout");
        ast.assignments().forEach(this::validateAssignment);
        ast.resultExpression().ifPresent(this::validateCompleteExpression);
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

    public Map<NodeId, SymbolBinding> symbolBindings() {
        return symbolBindings;
    }

    public Map<NodeId, ExpressionType> equalityOperandTypes() {
        return equalityOperandTypes;
    }

    public Map<NodeId, FunctionDescriptor> functionBindings() {
        return functionBindings;
    }

    public Map<NodeId, CollectionOperationBinding> collectionOperationBindings() {
        return collectionOperationBindings;
    }

    public FrameLayout frameLayout() {
        return frameLayout;
    }

    private void validateAssignment(AssignmentNode assignment) {
        validateCompleteExpression(assignment.expression());
        if (assignment.target() instanceof com.runestone.expeval_mk3.internal.ast.IdentifierAssignmentTargetNode target) {
            requireEntry(symbolBindings, target.id(), "assignment target binding");
            return;
        }
        if (assignment.target() instanceof com.runestone.expeval_mk3.internal.ast.DestructuringAssignmentTargetNode target) {
            target.elements().forEach(element -> requireEntry(symbolBindings, element.id(), "assignment target binding"));
            return;
        }
        throw new IllegalStateException("successful semantic model contains an unsupported assignment target");
    }

    private void validateCompleteExpression(ExpressionNode expression) {
        requireEntry(resolvedTypes, expression.id(), "resolved type");
        requireEntry(runtimeNullability, expression.id(), "runtime nullability");
        if (expression instanceof LiteralNode literal) {
            requireEntry(preparedValues, literal.id(), "prepared literal value");
            return;
        }
        if (expression instanceof IdentifierNode identifier) {
            requireEntry(symbolBindings, identifier.id(), "symbol binding");
            return;
        }
        if (expression instanceof CurrentItemNode currentItem) {
            requireEntry(symbolBindings, currentItem.id(), "current item binding");
            return;
        }
        if (expression instanceof CurrentTemporalValueNode) {
            return;
        }
        if (expression instanceof CollectionLiteralNode collection) {
            requireEntry(collectionShapes, collection.id(), "collection shape");
            collection.elements().forEach(this::validateCompleteExpression);
            return;
        }
        if (expression instanceof GroupedExpressionNode grouped) {
            validateCompleteExpression(grouped.expression());
            return;
        }
        if (expression instanceof BinaryOperationNode binary) {
            if (binary.operator() == BinaryOperator.EQUAL || binary.operator() == BinaryOperator.NOT_EQUAL) {
                requireEntry(equalityOperandTypes, binary.id(), "equality binding");
            }
            validateCompleteExpression(binary.left());
            validateCompleteExpression(binary.right());
            return;
        }
        if (expression instanceof UnaryOperationNode unary) {
            validateCompleteExpression(unary.operand());
            return;
        }
        if (expression instanceof PostfixOperationNode postfix) {
            validateCompleteExpression(postfix.operand());
            return;
        }
        if (expression instanceof BetweenNode between) {
            validateCompleteExpression(between.value());
            validateCompleteExpression(between.lowerBound());
            validateCompleteExpression(between.upperBound());
            return;
        }
        if (expression instanceof MembershipNode membership) {
            validateCompleteExpression(membership.element());
            validateCompleteExpression(membership.collection());
            return;
        }
        if (expression instanceof NullCoalesceNode coalesce) {
            coalesce.operands().forEach(this::validateCompleteExpression);
            return;
        }
        if (expression instanceof ConditionalNode conditional) {
            conditional.branches().forEach(this::validateConditionalBranch);
            validateCompleteExpression(conditional.elseExpression());
            return;
        }
        if (expression instanceof FunctionCallNode functionCall) {
            requireEntry(functionBindings, functionCall.id(), "function binding");
            functionCall.arguments().forEach(argument -> {
                if (argument instanceof com.runestone.expeval_mk3.internal.ast.ExpressionCallArgument expressionArgument) {
                    validateCompleteExpression(expressionArgument.expression());
                }
            });
            return;
        }
        if (expression instanceof NavigationChainNode navigation) {
            validateCompleteExpression(navigation.receiver());
            navigation.links().forEach(this::validateNavigationLink);
            return;
        }
        throw new IllegalStateException("successful semantic model contains an unsupported expression node");
    }

    private void validateNavigationLink(NavigationLink link) {
        requireEntry(resolvedTypes, link.id(), "navigation link resolved type");
        requireEntry(runtimeNullability, link.id(), "navigation link runtime nullability");
        if (link instanceof FilterNavigationLink filter) {
            requireEntry(symbolBindings, filter.id(), "filter current item binding");
            validateCompleteExpression(filter.predicate());
            return;
        }
        if (link instanceof CallNavigationLink call) {
            requireEntry(collectionOperationBindings, call.id(), "collection operation binding");
            call.arguments().forEach(argument -> {
                if (argument instanceof com.runestone.expeval_mk3.internal.ast.ExpressionCallArgument expressionArgument) {
                    validateCompleteExpression(expressionArgument.expression());
                } else if (argument instanceof LambdaCallArgument lambdaArgument) {
                    requireEntry(symbolBindings, lambdaArgument.lambda().currentItem().id(), "lambda current item binding");
                    validateCompleteExpression(lambdaArgument.lambda().body());
                }
            });
        }
    }

    private void validateConditionalBranch(ConditionalBranchNode branch) {
        validateCompleteExpression(branch.condition());
        validateCompleteExpression(branch.consequence());
    }

    private static <K, V> void requireEntry(Map<K, V> values, K key, String description) {
        if (!values.containsKey(key)) {
            throw new IllegalStateException("successful semantic model is missing " + description + " for " + key);
        }
    }
}
