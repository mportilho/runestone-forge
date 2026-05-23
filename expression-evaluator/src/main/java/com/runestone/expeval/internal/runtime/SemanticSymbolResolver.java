package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.IssueCode;
import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.AssignmentNode;
import com.runestone.expeval.internal.ast.DestructuringAssignmentNode;
import com.runestone.expeval.internal.ast.IdentifierNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.internal.ast.SimpleAssignmentNode;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.UnknownType;
import com.runestone.expeval.types.VectorType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class SemanticSymbolResolver {

    private final ExternalSymbolCatalog externalSymbolCatalog;
    private final Map<NodeId, SymbolRef> symbolByNodeId;
    private final Map<SymbolRef, List<IdentifierNode>> identifierUsages;
    private final Map<SymbolRef, List<AssignmentNode>> assignmentUsages;
    private final Map<String, SymbolRef> externalSymbolsByName;
    private final Map<String, SymbolRef> internalSymbolsByName;
    private final Map<SymbolRef, ResolvedType> internalTypes;
    private final SemanticErrorReporter errorReporter;

    SemanticSymbolResolver(
            ExternalSymbolCatalog externalSymbolCatalog,
            Map<NodeId, SymbolRef> symbolByNodeId,
            Map<SymbolRef, List<IdentifierNode>> identifierUsages,
            Map<SymbolRef, List<AssignmentNode>> assignmentUsages,
            Map<String, SymbolRef> externalSymbolsByName,
            Map<String, SymbolRef> internalSymbolsByName,
            Map<SymbolRef, ResolvedType> internalTypes,
            SemanticErrorReporter errorReporter) {
        this.externalSymbolCatalog = Objects.requireNonNull(externalSymbolCatalog, "externalSymbolCatalog");
        this.symbolByNodeId = Objects.requireNonNull(symbolByNodeId, "symbolByNodeId");
        this.identifierUsages = Objects.requireNonNull(identifierUsages, "identifierUsages");
        this.assignmentUsages = Objects.requireNonNull(assignmentUsages, "assignmentUsages");
        this.externalSymbolsByName = Objects.requireNonNull(externalSymbolsByName, "externalSymbolsByName");
        this.internalSymbolsByName = Objects.requireNonNull(internalSymbolsByName, "internalSymbolsByName");
        this.internalTypes = Objects.requireNonNull(internalTypes, "internalTypes");
        this.errorReporter = Objects.requireNonNull(errorReporter, "errorReporter");
    }

    void collectInternalSymbols(List<AssignmentNode> assignments) {
        for (AssignmentNode assignment : assignments) {
            switch (assignment) {
                case SimpleAssignmentNode simpleAssignment -> registerInternal(simpleAssignment.targetName());
                case DestructuringAssignmentNode destructuringAssignment ->
                        destructuringAssignment.targetNames().forEach(this::registerInternal);
            }
        }
    }

    void resolveSimpleAssignment(SimpleAssignmentNode assignment, ResolvedType valueType) {
        SymbolRef symbolRef = internalSymbolsByName.get(assignment.targetName());
        symbolByNodeId.put(assignment.nodeId(), symbolRef);
        assignmentUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(assignment);
        internalTypes.put(symbolRef, valueType);
    }

    ResolvedType resolveDestructuringAssignment(DestructuringAssignmentNode assignment, ResolvedType valueType) {
        for (String targetName : assignment.targetNames()) {
            SymbolRef symbolRef = internalSymbolsByName.get(targetName);
            assignmentUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(assignment);
            internalTypes.putIfAbsent(symbolRef, UnknownType.INSTANCE);
        }
        return valueType == UnknownType.INSTANCE ? UnknownType.INSTANCE : VectorType.INSTANCE;
    }

    ResolvedType resolveIdentifier(IdentifierNode node, @Nullable ResolvedType filterElementType) {
        if (LanguageSymbols.CURRENT_ELEMENT.equals(node.name())) {
            if (filterElementType == null) {
                errorReporter.error(
                        IssueCode.INVALID_CURRENT_ELEMENT,
                        "'@' may only be used inside a filter predicate [?(...)]",
                        node.sourceSpan());
                return UnknownType.INSTANCE;
            }
            return filterElementType;
        }

        SymbolRef symbolRef = internalSymbolsByName.get(node.name());
        if (symbolRef != null) {
            symbolByNodeId.put(node.nodeId(), symbolRef);
            identifierUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(node);
            return internalTypes.getOrDefault(symbolRef, UnknownType.INSTANCE);
        }

        symbolRef = externalSymbolsByName.computeIfAbsent(node.name(), name -> new SymbolRef(name, SymbolKind.EXTERNAL));
        symbolByNodeId.put(node.nodeId(), symbolRef);
        identifierUsages.computeIfAbsent(symbolRef, ignored -> new ArrayList<>()).add(node);
        ExternalSymbolDescriptor descriptor = externalSymbolCatalog.findOrNull(node.name());
        return descriptor != null ? descriptor.declaredType() : UnknownType.INSTANCE;
    }

    ResolvedType resolveRootType(PropertyChainNode node, @Nullable ResolvedType filterElementType) {
        if (LanguageSymbols.CURRENT_ELEMENT.equals(node.rootIdentifier())) {
            return filterElementType != null ? filterElementType : UnknownType.INSTANCE;
        }
        SymbolRef symbolRef = internalSymbolsByName.get(node.rootIdentifier());
        if (symbolRef != null) {
            symbolByNodeId.put(node.nodeId(), symbolRef);
            return internalTypes.getOrDefault(symbolRef, UnknownType.INSTANCE);
        }

        symbolRef = externalSymbolsByName.computeIfAbsent(
                node.rootIdentifier(), name -> new SymbolRef(name, SymbolKind.EXTERNAL));
        symbolByNodeId.put(node.nodeId(), symbolRef);
        ExternalSymbolDescriptor descriptor = externalSymbolCatalog.findOrNull(node.rootIdentifier());
        return descriptor != null ? descriptor.declaredType() : UnknownType.INSTANCE;
    }

    private void registerInternal(String name) {
        internalSymbolsByName.computeIfAbsent(name, ignored -> new SymbolRef(name, SymbolKind.INTERNAL));
    }

}
