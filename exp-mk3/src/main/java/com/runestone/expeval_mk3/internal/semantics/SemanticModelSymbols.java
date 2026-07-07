package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.internal.ast.NodeId;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SemanticModelSymbols(
        Map<NodeId, ExpressionType> resolvedTypes,
        Map<NodeId, NumericKind> numericKinds,
        List<ResidualTypeCheck> residualTypeChecks,
        Map<NodeId, ResolvedSymbol> symbolByNodeId,
        ResolvedSymbolSets symbolSets,
        FrameLayout frameLayout) {

    public SemanticModelSymbols {
        Objects.requireNonNull(resolvedTypes, "resolvedTypes");
        resolvedTypes = Map.copyOf(resolvedTypes);
        Objects.requireNonNull(numericKinds, "numericKinds");
        numericKinds = Map.copyOf(numericKinds);
        Objects.requireNonNull(residualTypeChecks, "residualTypeChecks");
        residualTypeChecks = List.copyOf(residualTypeChecks);
        Objects.requireNonNull(symbolByNodeId, "symbolByNodeId");
        symbolByNodeId = Map.copyOf(symbolByNodeId);
        Objects.requireNonNull(symbolSets, "symbolSets");
        Objects.requireNonNull(frameLayout, "frameLayout");
    }

    public static SemanticModelSymbols empty() {
        return new SemanticModelSymbols(
                Map.of(),
                Map.of(),
                List.of(),
                Map.of(),
                ResolvedSymbolSets.empty(),
                new FrameLayout(List.of()));
    }

    public List<ResolvedSymbol> internalSymbols() {
        return symbolSets.internalSymbols();
    }

    public List<ResolvedSymbol> externalSymbols() {
        return symbolSets.externalSymbols();
    }

    public List<ResolvedSymbol> currentItemSymbols() {
        return symbolSets.currentItemSymbols();
    }
}
