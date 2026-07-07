package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionType;
import com.runestone.expeval_mk3.internal.ast.ExpressionSemanticTree;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticSeverity;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record SemanticModel(
        ExpressionSemanticTree sourceTree,
        ExpressionEnvironment environment,
        List<ExpressionDiagnostic> diagnostics,
        SemanticModelSymbols symbols) {

    public SemanticModel(
            ExpressionSemanticTree sourceTree,
            ExpressionEnvironment environment,
            List<ExpressionDiagnostic> diagnostics) {
        this(sourceTree, environment, diagnostics, SemanticModelSymbols.empty());
    }

    public SemanticModel {
        Objects.requireNonNull(sourceTree, "sourceTree");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(diagnostics, "diagnostics");
        diagnostics = List.copyOf(diagnostics);
        Objects.requireNonNull(symbols, "symbols");
        if (diagnostics.stream().anyMatch(diagnostic -> diagnostic.severity() == DiagnosticSeverity.ERROR)) {
            throw new IllegalArgumentException("semantic model cannot contain error diagnostics");
        }
    }

    public Map<NodeId, ExpressionType> resolvedTypes() {
        return symbols.resolvedTypes();
    }

    public Map<NodeId, NumericKind> numericKinds() {
        return symbols.numericKinds();
    }

    public Map<NodeId, PreparedSemanticValue> preparedValues() {
        return symbols.preparedValues();
    }

    public List<ResidualTypeCheck> residualTypeChecks() {
        return symbols.residualTypeChecks();
    }

    public Map<NodeId, ResolvedFunctionBinding> functionBindings() {
        return symbols.functionBindings();
    }

    public Map<NodeId, ResolvedSymbol> symbolByNodeId() {
        return symbols.symbolByNodeId();
    }

    public List<ResolvedSymbol> internalSymbols() {
        return symbols.internalSymbols();
    }

    public List<ResolvedSymbol> externalSymbols() {
        return symbols.externalSymbols();
    }

    public List<ResolvedSymbol> currentItemSymbols() {
        return symbols.currentItemSymbols();
    }

    public FrameLayout frameLayout() {
        return symbols.frameLayout();
    }
}
