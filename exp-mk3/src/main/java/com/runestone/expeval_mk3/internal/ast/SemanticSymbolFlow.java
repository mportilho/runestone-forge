package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionType;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SemanticSymbolFlow(
        List<AssignmentSymbolFlow> assignments,
        List<SymbolReadSource> resultReads,
        Map<NodeId, ExpressionType> knownExpressionTypes,
        CurrentItemFlow currentItems) {

    public SemanticSymbolFlow {
        Objects.requireNonNull(assignments, "assignments");
        assignments = List.copyOf(assignments);
        Objects.requireNonNull(resultReads, "resultReads");
        resultReads = List.copyOf(resultReads);
        Objects.requireNonNull(knownExpressionTypes, "knownExpressionTypes");
        knownExpressionTypes = Map.copyOf(knownExpressionTypes);
        Objects.requireNonNull(currentItems, "currentItems");
    }

    public List<CurrentItemSource> currentItemSources() {
        return currentItems.sources();
    }

    public int maxCurrentItemDepth() {
        return currentItems.maxDepth();
    }
}
