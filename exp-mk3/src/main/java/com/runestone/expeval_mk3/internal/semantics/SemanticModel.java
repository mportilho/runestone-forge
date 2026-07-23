package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.api.RuntimeNullability;
import com.runestone.expeval_mk3.internal.ast.BinaryOperationNode;
import com.runestone.expeval_mk3.internal.ast.CollectionLiteralNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.ExpressionNode;
import com.runestone.expeval_mk3.internal.ast.GroupedExpressionNode;
import com.runestone.expeval_mk3.internal.ast.IdentifierNode;
import com.runestone.expeval_mk3.internal.ast.LiteralNode;
import com.runestone.expeval_mk3.internal.ast.NodeId;

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
    private final FrameLayout frameLayout;

    SemanticModel(
            ExpressionFileNode ast,
            Map<NodeId, ExpressionType> resolvedTypes,
            Map<NodeId, RuntimeNullability> runtimeNullability,
            Map<NodeId, Object> preparedValues,
            Map<NodeId, CollectionShape> collectionShapes,
            Map<NodeId, SymbolBinding> symbolBindings,
            Map<NodeId, ExpressionType> equalityOperandTypes,
            FrameLayout frameLayout) {
        this.ast = Objects.requireNonNull(ast, "ast");
        this.resolvedTypes = Map.copyOf(resolvedTypes);
        this.runtimeNullability = Map.copyOf(runtimeNullability);
        this.preparedValues = Map.copyOf(preparedValues);
        this.collectionShapes = Map.copyOf(collectionShapes);
        this.symbolBindings = Map.copyOf(symbolBindings);
        this.equalityOperandTypes = Map.copyOf(equalityOperandTypes);
        this.frameLayout = Objects.requireNonNull(frameLayout, "frameLayout");
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

    public FrameLayout frameLayout() {
        return frameLayout;
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
            requireEntry(equalityOperandTypes, binary.id(), "equality binding");
            validateCompleteExpression(binary.left());
            validateCompleteExpression(binary.right());
            return;
        }
        throw new IllegalStateException("successful semantic model contains an unsupported expression node");
    }

    private static <K, V> void requireEntry(Map<K, V> values, K key, String description) {
        if (!values.containsKey(key)) {
            throw new IllegalStateException("successful semantic model is missing " + description + " for " + key);
        }
    }
}
