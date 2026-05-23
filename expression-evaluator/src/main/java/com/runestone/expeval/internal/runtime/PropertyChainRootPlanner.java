package com.runestone.expeval.internal.runtime;

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
                    SymbolReadPlanner.currentElement(node.sourceSpan()),
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
        return SymbolReadPlanner.read(rootRef, node.sourceSpan(), foldContext);
    }

    record PropertyChainRootPlan(ExecutableNode root, ResolvedType rootType) {
    }
}
