package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.BinaryOperator;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.runtime.ExecutionScope;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SymbolBinding;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ExecutionPlanBuilder {

    public ExecutionPlan build(SemanticModel model, ExpressionEnvironment environment) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(environment, "environment");
        ExpressionNode result = model.ast().resultExpression().orElseThrow(
                () -> new IllegalArgumentException("semantic model must have a result expression"));
        List<ExternalBindingPlan> externalBindings = model.frameLayout().externalBindings().stream()
                .map(binding -> new ExternalBindingPlan(binding.symbol(), binding.frameSlot()))
                .toList();
        return new ExecutionPlan(
                buildNode(result, model),
                externalBindings,
                model.frameLayout().frameSize(),
                environment.boundaryCoercion());
    }

    private ExecutableNode buildNode(ExpressionNode node, SemanticModel model) {
        if (node instanceof LiteralNode literal) {
            Object value = required(model.preparedValues(), literal.id(), "prepared literal value");
            return scope -> value;
        }
        if (node instanceof CollectionLiteralNode collection) {
            List<ExecutableNode> elements = collection.elements().stream()
                    .map(element -> buildNode(element, model))
                    .toList();
            return scope -> materialize(elements, scope);
        }
        if (node instanceof IdentifierNode identifier) {
            SymbolBinding binding = required(model.symbolBindings(), identifier.id(), "symbol binding");
            return scope -> scope.read(binding.frameSlot());
        }
        if (node instanceof GroupedExpressionNode grouped) {
            return buildNode(grouped.expression(), model);
        }
        if (node instanceof BinaryOperationNode binary) {
            ExecutableNode left = buildNode(binary.left(), model);
            ExecutableNode right = buildNode(binary.right(), model);
            ExpressionType operandType = required(
                    model.equalityOperandTypes(), binary.id(), "equality operand type");
            boolean negated = binary.operator() == BinaryOperator.NOT_EQUAL;
            return scope -> structuralEquals(left.execute(scope), right.execute(scope), operandType) != negated;
        }
        throw new IllegalArgumentException("unsupported planned node: " + node.getClass().getSimpleName());
    }

    private static List<Object> materialize(List<ExecutableNode> elements, ExecutionScope scope) {
        ArrayList<Object> values = new ArrayList<>(elements.size());
        for (ExecutableNode element : elements) {
            values.add(Objects.requireNonNull(element.execute(scope), "collection element"));
        }
        return List.copyOf(values);
    }

    private static boolean structuralEquals(Object left, Object right, ExpressionType type) {
        if (type == ScalarType.NUMBER) {
            return ((BigDecimal) left).compareTo((BigDecimal) right) == 0;
        }
        if (type instanceof CollectionType collectionType) {
            List<?> leftValues = (List<?>) left;
            List<?> rightValues = (List<?>) right;
            if (leftValues.size() != rightValues.size()) {
                return false;
            }
            for (int index = 0; index < leftValues.size(); index++) {
                if (!structuralEquals(leftValues.get(index), rightValues.get(index), collectionType.elementType())) {
                    return false;
                }
            }
            return true;
        }
        if (type instanceof MapType mapType) {
            Map<?, ?> leftValues = (Map<?, ?>) left;
            Map<?, ?> rightValues = (Map<?, ?>) right;
            if (!leftValues.keySet().equals(rightValues.keySet())) {
                return false;
            }
            for (Object key : leftValues.keySet()) {
                if (!structuralEquals(leftValues.get(key), rightValues.get(key), mapType.valueType())) {
                    return false;
                }
            }
            return true;
        }
        return left.equals(right);
    }

    private static <K, V> V required(Map<K, V> values, K key, String description) {
        V value = values.get(key);
        if (value == null) {
            throw new IllegalStateException("semantic model is missing " + description + " for " + key);
        }
        return value;
    }
}
