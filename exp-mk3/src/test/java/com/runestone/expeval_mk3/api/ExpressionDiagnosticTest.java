package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionDiagnosticTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 3, 1, 1);

    @Test
    void errorFactoryCarriesRequiredFieldsAndPrimarySpan() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.error(
                DiagnosticCategory.SEMANTIC, "SEMANTIC_UNKNOWN_SYMBOL", "Unknown symbol: x", SPAN);

        assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.SEMANTIC);
        assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.ERROR);
        assertThat(diagnostic.code()).isEqualTo("SEMANTIC_UNKNOWN_SYMBOL");
        assertThat(diagnostic.message()).isEqualTo("Unknown symbol: x");
        assertThat(diagnostic.primarySpan()).contains(SPAN);
        assertThat(diagnostic.relatedInformation()).isEmpty();
        assertThat(diagnostic.notes()).isEmpty();
        assertThat(diagnostic.suggestion()).isEmpty();
    }

    @Test
    void warningFactoryHasWarningSeverity() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.warning(
                DiagnosticCategory.SEMANTIC, "SEMANTIC_SYMBOL_SHADOWING", "Symbol shadows an outer binding", SPAN);

        assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.WARNING);
    }

    @Test
    void purelyExternalDiagnosticMayOmitPrimarySpan() {
        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.error(
                DiagnosticCategory.RUNTIME, "RUNTIME_EXTERNAL_FAILURE", "External provider failed", null);

        assertThat(diagnostic.primarySpan()).isEmpty();
    }

    @Test
    void builderCarriesRelatedInformationNotesAndSuggestion() {
        RelatedInformation related = new RelatedInformation("declared here", SPAN);

        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.builder(
                        DiagnosticCategory.SEMANTIC, DiagnosticSeverity.ERROR, "SEMANTIC_UNKNOWN_SYMBOL", "Unknown symbol: x")
                .primarySpan(SPAN)
                .relatedInformation(List.of(related))
                .notes(List.of("did you mean y?"))
                .suggestion("y")
                .build();

        assertThat(diagnostic.relatedInformation()).containsExactly(related);
        assertThat(diagnostic.notes()).containsExactly("did you mean y?");
        assertThat(diagnostic.suggestion()).contains("y");
    }

    @Test
    void relatedInformationRequiresMessageAndSpan() {
        assertThatThrownBy(() -> new RelatedInformation(null, SPAN))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new RelatedInformation("message", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructionRejectsNullCategorySeverityCodeOrMessage() {
        assertThatThrownBy(() -> ExpressionDiagnostic.error(null, "CODE", "message", SPAN))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExpressionDiagnostic.error(DiagnosticCategory.SEMANTIC, null, "message", SPAN))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> ExpressionDiagnostic.error(DiagnosticCategory.SEMANTIC, "CODE", null, SPAN))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void relatedInformationAndNotesAreImmutableSnapshots() {
        List<RelatedInformation> related = new ArrayList<>();
        related.add(new RelatedInformation("note", SPAN));
        List<String> notes = new ArrayList<>();
        notes.add("first");

        ExpressionDiagnostic diagnostic = ExpressionDiagnostic.builder(
                        DiagnosticCategory.SEMANTIC, DiagnosticSeverity.ERROR, "CODE", "message")
                .relatedInformation(related)
                .notes(notes)
                .build();

        related.add(new RelatedInformation("mutated", SPAN));
        notes.add("second");

        assertThat(diagnostic.relatedInformation()).hasSize(1);
        assertThat(diagnostic.notes()).hasSize(1);
        assertThatThrownBy(() -> diagnostic.relatedInformation().add(new RelatedInformation("x", SPAN)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> diagnostic.notes().add("x"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
