package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.internal.source.SourceSpan;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AssignmentSymbolFlow {

    private final List<SymbolReadSource> expressionReads;
    private final SymbolReadSource expressionRootRead;
    private final NodeId expressionNodeId;
    private final List<AssignedSymbolSource> targetSymbols;
    private final ExpressionType expressionType;
    private final SourceSpan sourceSpan;

    AssignmentSymbolFlow(
            List<SymbolReadSource> expressionReads,
            SymbolReadSource expressionRootRead,
            NodeId expressionNodeId,
            List<AssignedSymbolSource> targetSymbols,
            ExpressionType expressionType,
            SourceSpan sourceSpan) {
        Objects.requireNonNull(expressionReads, "expressionReads");
        this.expressionReads = List.copyOf(expressionReads);
        this.expressionRootRead = expressionRootRead;
        this.expressionNodeId = Objects.requireNonNull(expressionNodeId, "expressionNodeId");
        Objects.requireNonNull(targetSymbols, "targetSymbols");
        this.targetSymbols = List.copyOf(targetSymbols);
        this.expressionType = expressionType;
        this.sourceSpan = Objects.requireNonNull(sourceSpan, "sourceSpan");
    }

    public List<SymbolReadSource> expressionReads() {
        return expressionReads;
    }

    public Optional<SymbolReadSource> expressionRootRead() {
        return Optional.ofNullable(expressionRootRead);
    }

    public NodeId expressionNodeId() {
        return expressionNodeId;
    }

    public List<AssignedSymbolSource> targetSymbols() {
        return targetSymbols;
    }

    public Optional<ExpressionType> expressionType() {
        return Optional.ofNullable(expressionType);
    }

    public SourceSpan sourceSpan() {
        return sourceSpan;
    }
}
