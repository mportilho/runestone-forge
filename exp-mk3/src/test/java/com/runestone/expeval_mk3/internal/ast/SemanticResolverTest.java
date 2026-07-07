package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticSeverity;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionResult;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticResolverTest {

    private final SemanticResolver resolver = new SemanticResolver();

    @Test
    @DisplayName("non-empty expression files resolve to a planejable semantic model without diagnostics")
    void nonEmptyExpressionFilesResolveToSemanticModelWithoutDiagnostics() {
        ExpressionFileNode ast = AstTestSupport.build("1");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.diagnostics()).isEmpty();
        assertThat(result.model())
                .get()
                .satisfies(model -> {
                    assertThat(model.sourceTree()).isSameAs(ast);
                    assertThat(model.diagnostics()).isEmpty();
                });
    }

    @Test
    @DisplayName("semantic warnings coexist with a planejable semantic model")
    void semanticWarningsCoexistWithSemanticModel() {
        ExpressionFileNode ast = AstTestSupport.build("amount := 1; amount");
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("amount", ScalarType.NUMBER)
                .build();

        SemanticResolutionResult result = resolver.resolve(ast, environment);

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.model()).isPresent();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> {
                    assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.SEMANTIC);
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.SEMANTIC_EXTERNAL_SYMBOL_SHADOWED);
                    assertThat(diagnostic.severity()).isEqualTo(DiagnosticSeverity.WARNING);
                    assertThat(diagnostic.span()).isEqualTo(new SourceSpan(0, 6, 1, 1));
                });
        assertThat(result.model())
                .get()
                .extracting(model -> model.diagnostics())
                .isEqualTo(result.diagnostics());
    }

    @Test
    @DisplayName("empty expression files resolve to a stable semantic error and no model")
    void emptyExpressionFilesResolveToStableSemanticErrorAndNoModel() {
        ExpressionFileNode ast = AstTestSupport.build("");

        SemanticResolutionResult result = resolver.resolve(ast, ExpressionEnvironment.standard());

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.model()).isEmpty();
        assertThat(result.diagnostics())
                .singleElement()
                .satisfies(diagnostic -> assertThat(diagnostic)
                        .isEqualTo(ExpressionDiagnostic.error(
                                DiagnosticCategory.SEMANTIC,
                                DiagnosticCode.SEMANTIC_EMPTY_EXPRESSION_FILE,
                                "Expression file must contain at least one assignment or result expression",
                                new SourceSpan(0, 0, 1, 1))));
    }
}
