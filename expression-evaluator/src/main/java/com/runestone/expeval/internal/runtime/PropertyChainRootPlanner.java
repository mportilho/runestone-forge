package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.AuditEvent;
import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.internal.LanguageSymbols;
import com.runestone.expeval.internal.ast.PropertyChainNode;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.UnknownType;

final class PropertyChainRootPlanner {

    private PropertyChainRootPlanner() {
    }

    static PropertyChainRootPlan build(
            PropertyChainNode node,
            SemanticModel model,
            ExternalSymbolCatalog externalSymbolCatalog,
            ConstantFoldContext foldContext) {
        if (LanguageSymbols.CURRENT_ELEMENT.equals(node.rootIdentifier())) {
            return new PropertyChainRootPlan(
                    new ExecutableIdentifier(new SymbolRef(LanguageSymbols.CURRENT_ELEMENT, SymbolKind.EXTERNAL), node.sourceSpan()),
                    UnknownType.INSTANCE
            );
        }

        SymbolRef rootRef = model.findSymbol(node.nodeId())
                .orElseThrow(() -> new IllegalStateException(
                        "missing symbol for property chain '" + node.rootIdentifier() + "'"));
        ExecutableNode root = buildRootNode(rootRef, node, foldContext);
        ResolvedType rootType = PropertyChainRootTypeResolver.resolve(rootRef, model, externalSymbolCatalog);
        return new PropertyChainRootPlan(root, rootType);
    }

    private static ExecutableNode buildRootNode(
            SymbolRef rootRef,
            PropertyChainNode node,
            ConstantFoldContext foldContext) {
        if (foldContext.symbols.containsKey(rootRef)) {
            Object value = foldContext.symbols.get(rootRef);
            foldContext.variableReads.add(new AuditEvent.VariableRead(rootRef.name(), false, value));
            return new ExecutableLiteral(value);
        }
        return new ExecutableIdentifier(rootRef, node.sourceSpan());
    }

    record PropertyChainRootPlan(ExecutableNode root, ResolvedType rootType) {
    }
}
