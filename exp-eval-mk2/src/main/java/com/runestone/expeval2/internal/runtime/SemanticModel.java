package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.internal.ast.AssignmentNode;
import com.runestone.expeval2.internal.ast.ExpressionFileNode;
import com.runestone.expeval2.internal.ast.IdentifierNode;
import com.runestone.expeval2.internal.ast.NodeId;
import com.runestone.expeval2.types.ResolvedType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record SemanticModel(
        ExpressionFileNode ast,
        Map<NodeId, ResolvedType> resolvedTypes,
        Map<NodeId, SymbolRef> symbolByNodeId,
        Map<SymbolRef, List<IdentifierNode>> identifierUsages,
        Map<SymbolRef, List<AssignmentNode>> assignmentUsages,
        Map<String, SymbolRef> externalSymbolsByName,
        Map<String, SymbolRef> internalSymbolsByName,
        Map<NodeId, ResolvedFunctionBinding> functionBindings,
        List<SemanticIssue> issues
) {

    public SemanticModel {
        Objects.requireNonNull(ast, "ast must not be null");
        resolvedTypes = Map.copyOf(Objects.requireNonNull(resolvedTypes, "resolvedTypes must not be null"));
        symbolByNodeId = Map.copyOf(Objects.requireNonNull(symbolByNodeId, "symbolByNodeId must not be null"));
        identifierUsages = immutableLists(Objects.requireNonNull(identifierUsages, "identifierUsages must not be null"));
        assignmentUsages = immutableLists(Objects.requireNonNull(assignmentUsages, "assignmentUsages must not be null"));
        externalSymbolsByName = Map.copyOf(Objects.requireNonNull(externalSymbolsByName, "externalSymbolsByName must not be null"));
        internalSymbolsByName = Map.copyOf(Objects.requireNonNull(internalSymbolsByName, "internalSymbolsByName must not be null"));
        functionBindings = Map.copyOf(Objects.requireNonNull(functionBindings, "functionBindings must not be null"));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues must not be null"));
    }

    public Optional<ResolvedType> findResolvedType(NodeId nodeId) {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        return Optional.ofNullable(resolvedTypes.get(nodeId));
    }

    public Optional<SymbolRef> findSymbol(NodeId nodeId) {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        return Optional.ofNullable(symbolByNodeId.get(nodeId));
    }

    public Optional<ResolvedFunctionBinding> findFunctionBinding(NodeId nodeId) {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
        return Optional.ofNullable(functionBindings.get(nodeId));
    }

    public boolean hasErrors() {
        return issues.stream().anyMatch(issue -> issue.severity() == SemanticIssueSeverity.ERROR);
    }

    private static <K, V> Map<K, List<V>> immutableLists(Map<K, List<V>> values) {
        Map<K, List<V>> copy = new LinkedHashMap<>();
        values.forEach((key, list) -> copy.put(key, List.copyOf(list)));
        return Map.copyOf(copy);
    }
}
