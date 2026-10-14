package com.runestone.expeval.api;

import java.util.List;

/**
 * Conservative cache-admission estimate for source-controlled compilation payload. Units are calibrated
 * against JVM object layouts, but intentionally are not advertised as exact heap bytes. The estimator
 * includes the exact source-key text, resident result diagnostics, downloaded plan shape, and folded
 * constants while excluding trusted shared environment services.
 */
final class CompilationRetainedWeight {

    static final int ENTRY_UNITS = 96;
    static final int RESULT_UNITS = 64;
    static final int COMPILED_EXPRESSION_UNITS = 64;
    static final int DIAGNOSTIC_UNITS = 128;
    private static final int LIST_UNITS = 48;
    private static final int REFERENCE_UNITS = 8;
    private static final int STRING_UNITS = 48;

    private CompilationRetainedWeight() {
    }

    static int estimate(String source, ExpressionCompilationResult result) {
        long units = ENTRY_UNITS + stringUnits(source) + RESULT_UNITS + diagnosticsUnits(resultDiagnostics(result));
        if (result instanceof ExpressionCompilationResult.Success success) {
            CompiledExpression compiledExpression = success.compiledExpression();
            units += COMPILED_EXPRESSION_UNITS + compiledExpression.estimatedRetainedPlanWeight();
            // CompiledExpression keeps its own immutable diagnostic list, but it shares diagnostic instances
            // with the public result, so only the list structure is charged a second time.
            units += listUnits(compiledExpression.compilationDiagnostics().size());
        }
        return saturatingInt(units);
    }

    private static List<ExpressionDiagnostic> resultDiagnostics(ExpressionCompilationResult result) {
        return result instanceof ExpressionCompilationResult.Success success
                ? success.diagnostics()
                : ((ExpressionCompilationResult.Failure) result).diagnostics();
    }

    private static long diagnosticsUnits(List<ExpressionDiagnostic> diagnostics) {
        long units = listUnits(diagnostics.size());
        for (ExpressionDiagnostic diagnostic : diagnostics) {
            units += DIAGNOSTIC_UNITS;
            units += stringUnits(diagnostic.code());
            units += stringUnits(diagnostic.message());
            units += listUnits(diagnostic.notes().size());
            for (String note : diagnostic.notes()) {
                units += stringUnits(note);
            }
            units += listUnits(diagnostic.relatedInformation().size());
            for (RelatedInformation related : diagnostic.relatedInformation()) {
                units += DIAGNOSTIC_UNITS + stringUnits(related.message());
            }
            if (diagnostic.suggestion().isPresent()) {
                units += stringUnits(diagnostic.suggestion().orElseThrow());
            }
        }
        return units;
    }

    private static long listUnits(int size) {
        return LIST_UNITS + (long) size * REFERENCE_UNITS;
    }

    private static long stringUnits(String value) {
        return STRING_UNITS + (long) value.length() * Character.BYTES;
    }

    private static int saturatingInt(long units) {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, units));
    }
}
