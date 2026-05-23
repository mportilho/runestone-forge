package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.catalog.ExternalSymbolDescriptor;
import com.runestone.expeval.internal.ast.AssignmentNode;
import com.runestone.expeval.internal.ast.DestructuringAssignmentNode;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ResolvedTypes;
import com.runestone.expeval.types.UnknownType;

import java.util.List;

final class PropertyChainRootTypeResolver {

    private PropertyChainRootTypeResolver() {
    }

    static ResolvedType resolve(
            SymbolRef root,
            SemanticModel model,
            ExternalSymbolCatalog externalSymbolCatalog) {
        if (root.kind() == SymbolKind.EXTERNAL) {
            ExternalSymbolDescriptor descriptor = externalSymbolCatalog.findOrNull(root.name());
            return descriptor == null ? UnknownType.INSTANCE : descriptor.declaredType();
        }
        return resolveInternalSymbolType(root, model);
    }

    private static ResolvedType resolveInternalSymbolType(SymbolRef root, SemanticModel model) {
        List<AssignmentNode> assignments = model.assignmentUsages().get(root);
        if (assignments == null || assignments.isEmpty()) {
            return UnknownType.INSTANCE;
        }
        ResolvedType resolvedType = UnknownType.INSTANCE;
        for (AssignmentNode assignment : assignments) {
            if (assignment instanceof DestructuringAssignmentNode) {
                return UnknownType.INSTANCE;
            }
            resolvedType = ResolvedTypes.merge(
                    resolvedType,
                    model.findResolvedType(assignment.nodeId()).orElse(UnknownType.INSTANCE)
            );
        }
        return resolvedType;
    }
}
