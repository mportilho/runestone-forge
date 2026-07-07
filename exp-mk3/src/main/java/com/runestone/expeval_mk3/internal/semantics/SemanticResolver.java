package com.runestone.expeval_mk3.internal.semantics;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.internal.ast.AssignedSymbolSource;
import com.runestone.expeval_mk3.internal.ast.ExpressionSemanticTree;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class SemanticResolver {

    public SemanticResolutionResult resolve(ExpressionSemanticTree sourceTree, ExpressionEnvironment environment) {
        Objects.requireNonNull(sourceTree, "sourceTree");
        Objects.requireNonNull(environment, "environment");

        List<ExpressionDiagnostic> diagnostics = new ArrayList<>();
        if (sourceTree.isEmptyFile()) {
            diagnostics.add(ExpressionDiagnostic.error(
                    DiagnosticCategory.SEMANTIC,
                    DiagnosticCode.SEMANTIC_EMPTY_EXPRESSION_FILE,
                    "Expression file must contain at least one assignment or result expression",
                    sourceTree.sourceSpan()));
            return SemanticResolutionResult.withoutModel(diagnostics);
        }

        for (AssignedSymbolSource assignedSymbol : sourceTree.assignedSymbols()) {
            if (environment.externalSymbols().contains(assignedSymbol.name())) {
                diagnostics.add(ExpressionDiagnostic.warning(
                        DiagnosticCategory.SEMANTIC,
                        DiagnosticCode.SEMANTIC_EXTERNAL_SYMBOL_SHADOWED,
                        "Internal symbol shadows external symbol: " + assignedSymbol.name(),
                        assignedSymbol.sourceSpan()));
            }
        }

        return SemanticResolutionResult.withModel(new SemanticModel(sourceTree, environment, diagnostics));
    }
}
