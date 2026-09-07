package com.runestone.expeval_mk3.internal.diagnostics;

import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.RelatedInformation;
import com.runestone.expeval_mk3.api.SourceSpan;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Internal construction and ordering seam for every production expression diagnostic. */
public final class ExpressionDiagnostics {

    public static final Comparator<ExpressionDiagnostic> CANONICAL_ORDER = Comparator
            .comparing((ExpressionDiagnostic diagnostic) -> diagnostic.primarySpan().isEmpty())
            .thenComparingInt(diagnostic -> diagnostic.primarySpan().map(SourceSpan::offset).orElse(Integer.MAX_VALUE))
            .thenComparing(ExpressionDiagnostic::severity)
            .thenComparing(ExpressionDiagnostic::category)
            .thenComparing(ExpressionDiagnostic::code)
            .thenComparingInt(diagnostic -> diagnostic.primarySpan().map(SourceSpan::endOffset).orElse(Integer.MAX_VALUE));

    private ExpressionDiagnostics() {
    }

    public static ExpressionDiagnostic create(DiagnosticCode code, String message, SourceSpan primarySpan) {
        return create(code, message, primarySpan, null);
    }

    public static ExpressionDiagnostic create(
            DiagnosticCode code, String message, SourceSpan primarySpan, String suggestion) {
        return create(code, message, primarySpan, suggestion, List.of());
    }

    public static ExpressionDiagnostic createWithRelatedInformation(
            DiagnosticCode code,
            String message,
            SourceSpan primarySpan,
            List<RelatedInformation> relatedInformation) {
        return create(code, message, primarySpan, null, relatedInformation);
    }

    private static ExpressionDiagnostic create(
            DiagnosticCode code,
            String message,
            SourceSpan primarySpan,
            String suggestion,
            List<RelatedInformation> relatedInformation) {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(relatedInformation, "relatedInformation");
        validateSpan(code, primarySpan);
        validateSuggestion(code, suggestion);
        ExpressionDiagnostic.Builder builder = ExpressionDiagnostic.builder(
                        code.category(), code.severity(), code.code(), message)
                .primarySpan(primarySpan)
                .relatedInformation(relatedInformation);
        if (suggestion != null) {
            builder.suggestion(suggestion);
        }
        return builder.build();
    }

    public static List<ExpressionDiagnostic> canonicalCopy(List<ExpressionDiagnostic> diagnostics) {
        return diagnostics.stream().sorted(CANONICAL_ORDER).toList();
    }

    private static void validateSpan(DiagnosticCode code, SourceSpan span) {
        if (code.sourceSpanPolicy() == SourceSpanPolicy.REQUIRED && span == null) {
            throw new IllegalArgumentException(code.code() + " requires a primary source span");
        }
        if (code.sourceSpanPolicy() == SourceSpanPolicy.FORBIDDEN && span != null) {
            throw new IllegalArgumentException(code.code() + " forbids a primary source span");
        }
    }

    private static void validateSuggestion(DiagnosticCode code, String suggestion) {
        boolean present = suggestion != null && !suggestion.isBlank();
        if (code.suggestionPolicy() == SuggestionPolicy.REQUIRED && !present) {
            throw new IllegalArgumentException(code.code() + " requires a suggestion");
        }
        if (code.suggestionPolicy() == SuggestionPolicy.FORBIDDEN && suggestion != null) {
            throw new IllegalArgumentException(code.code() + " forbids a suggestion");
        }
    }
}
