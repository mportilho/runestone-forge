package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CalculationKey;
import com.runestone.expeval_mk3.api.CalculationKind;
import com.runestone.expeval_mk3.api.CurrentTemporalValue;
import com.runestone.expeval_mk3.api.FunctionDescriptor;
import com.runestone.expeval_mk3.internal.ast.BetweenNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.CallArgument;
import com.runestone.expeval_mk3.internal.ast.CallNavigationLink;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ConditionalNode;
import com.runestone.expeval_mk3.internal.ast.CurrentItemNode;
import com.runestone.expeval_mk3.internal.ast.CurrentTemporalValueNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionCallArgument;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.FilterNavigationLink;
import com.runestone.expeval_mk3.internal.ast.FunctionCallNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.MembershipNode;
import com.runestone.expeval_mk3.internal.ast.NavigationChainNode;
import com.runestone.expeval_mk3.internal.ast.NavigationLink;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.NullCoalesceNode;
import com.runestone.expeval_mk3.internal.ast.PostfixOperationNode;
import com.runestone.expeval_mk3.internal.ast.UnaryOperationNode;
import com.runestone.expeval_mk3.internal.runtime.ConstantFolder;
import com.runestone.expeval_mk3.internal.semantics.CollectionOperationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredMethodNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.RegisteredPropertyNavigationBinding;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Assignment-first, evaluation-order inventory of calculation points visible to one top-level execution. */
final class CalculationPointInventory {

    static final int INACTIVE_SLOT = -1;

    private final Map<NodeId, Integer> slotsByNodeId;
    private final List<CalculationKey> keys;
    private final int assignmentPointCount;

    private CalculationPointInventory(
            Map<NodeId, Integer> slotsByNodeId,
            List<CalculationKey> keys,
            int assignmentPointCount) {
        this.slotsByNodeId = Map.copyOf(slotsByNodeId);
        this.keys = List.copyOf(keys);
        this.assignmentPointCount = assignmentPointCount;
    }

    static CalculationPointInventory build(SemanticModel model) {
        Builder builder = new Builder(model);
        model.ast().assignments().forEach(assignment -> builder.visit(assignment.expression()));
        int assignmentPointCount = builder.keys.size();
        model.ast().resultExpression().ifPresent(builder::visit);
        return new CalculationPointInventory(builder.slotsByNodeId, builder.keys, assignmentPointCount);
    }

    int slot(NodeId nodeId) {
        return slotsByNodeId.getOrDefault(nodeId, INACTIVE_SLOT);
    }

    List<CalculationKey> keys() {
        return keys;
    }

    List<CalculationKey> assignmentKeys() {
        return keys.subList(0, assignmentPointCount);
    }

    private static final class Builder {

        private final SemanticModel model;
        private final Map<NodeId, Integer> slotsByNodeId = new HashMap<>();
        private final List<CalculationKey> keys = new ArrayList<>();

        private Builder(SemanticModel model) {
            this.model = model;
        }

        private void visit(ExpressionNode node) {
            switch (node) {
                case LiteralNode ignored -> {
                }
                case IdentifierNode ignored -> {
                }
                case CurrentItemNode ignored -> {
                }
                case CurrentTemporalValueNode temporal -> add(
                        temporal.id(),
                        new CalculationKey(
                                temporal.id().value(), temporal.sourceSpan(), CalculationKind.CURRENT_TEMPORAL,
                                temporalName(temporal)));
                case GroupedExpressionNode grouped -> visit(grouped.expression());
                case CollectionLiteralNode collection -> collection.elements().forEach(this::visit);
                case BinaryOperationNode binary -> {
                    visit(binary.left());
                    visit(binary.right());
                }
                case UnaryOperationNode unary -> visit(unary.operand());
                case PostfixOperationNode postfix -> visit(postfix.operand());
                case BetweenNode between -> {
                    visit(between.value());
                    visit(between.lowerBound());
                    visit(between.upperBound());
                }
                case MembershipNode membership -> {
                    visit(membership.element());
                    visit(membership.collection());
                }
                case NullCoalesceNode coalesce -> coalesce.operands().forEach(this::visit);
                case ConditionalNode conditional -> {
                    conditional.branches().forEach(branch -> {
                        visit(branch.condition());
                        visit(branch.consequence());
                    });
                    visit(conditional.elseExpression());
                }
                case FunctionCallNode function -> visitFunction(function);
                case NavigationChainNode navigation -> visitNavigation(navigation);
            }
        }

        private void visitFunction(FunctionCallNode function) {
            visitArguments(function.arguments());
            FunctionDescriptor descriptor = BindingLookup.required(
                    model.functionBindings(), function.id(), "function binding");
            if (!ConstantFolder.isElidableAssertion(descriptor)) {
                add(function.id(), new CalculationKey(
                        function.id().value(), function.sourceSpan(), CalculationKind.FUNCTION,
                        descriptor.languageName()));
            }
        }

        private void visitNavigation(NavigationChainNode navigation) {
            visit(navigation.receiver());
            for (NavigationLink link : navigation.links()) {
                Object binding = model.navigationBindings().get(link.id());
                if (binding instanceof CollectionOperationBinding || link instanceof FilterNavigationLink) {
                    continue;
                }
                if (binding instanceof RegisteredPropertyNavigationBinding propertyBinding) {
                    add(link.id(), new CalculationKey(
                            link.id().value(), link.sourceSpan(), CalculationKind.PROPERTY,
                            propertyBinding.descriptor().name()));
                    continue;
                }
                if (binding instanceof RegisteredMethodNavigationBinding methodBinding) {
                    CallNavigationLink call = (CallNavigationLink) link;
                    visitArguments(call.arguments());
                    add(link.id(), new CalculationKey(
                            link.id().value(), link.sourceSpan(), CalculationKind.METHOD,
                            methodBinding.descriptor().languageName()));
                }
            }
        }

        private void visitArguments(List<CallArgument> arguments) {
            for (CallArgument argument : arguments) {
                if (argument instanceof ExpressionCallArgument expressionArgument) {
                    visit(expressionArgument.expression());
                }
            }
        }

        private void add(NodeId nodeId, CalculationKey key) {
            int ordinal = keys.size();
            if (slotsByNodeId.put(nodeId, ordinal) != null) {
                throw new IllegalStateException("duplicate calculation node: " + nodeId);
            }
            keys.add(key);
        }

        private static String temporalName(CurrentTemporalValueNode temporal) {
            return switch (temporal.kind()) {
                case DATE -> CurrentTemporalValue.DATE.simpleName();
                case TIME -> CurrentTemporalValue.TIME.simpleName();
                case DATE_TIME -> CurrentTemporalValue.DATETIME.simpleName();
            };
        }
    }
}
