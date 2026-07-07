package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionType;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class AssignmentSymbolFlow {

    private final List<SymbolReadSource> expressionReads;
    private final SymbolReadSource expressionRootRead;
    private final List<AssignedSymbolSource> targetSymbols;
    private final ExpressionType expressionType;

    AssignmentSymbolFlow(
            List<SymbolReadSource> expressionReads,
            SymbolReadSource expressionRootRead,
            List<AssignedSymbolSource> targetSymbols,
            ExpressionType expressionType) {
        Objects.requireNonNull(expressionReads, "expressionReads");
        this.expressionReads = List.copyOf(expressionReads);
        this.expressionRootRead = expressionRootRead;
        Objects.requireNonNull(targetSymbols, "targetSymbols");
        this.targetSymbols = List.copyOf(targetSymbols);
        this.expressionType = expressionType;
    }

    public List<SymbolReadSource> expressionReads() {
        return expressionReads;
    }

    public Optional<SymbolReadSource> expressionRootRead() {
        return Optional.ofNullable(expressionRootRead);
    }

    public List<AssignedSymbolSource> targetSymbols() {
        return targetSymbols;
    }

    public Optional<ExpressionType> expressionType() {
        return Optional.ofNullable(expressionType);
    }
}
