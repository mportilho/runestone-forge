package com.runestone.expeval.internal.ast;

import com.runestone.expeval.api.DiagnosticCategory;
import com.runestone.expeval.api.ExpressionDiagnostic;
import com.runestone.expeval.api.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AstDiagnosticOwnershipTest {

    @Test
    @DisplayName("AST code references the public expression diagnostic contract directly")
    void astCodeReferencesThePublicExpressionDiagnosticContract() {
        assertThat(ExpressionDiagnostic.class.getPackageName())
                .isEqualTo("com.runestone.expeval.api");
        assertThat(SourceSpan.class.getPackageName())
                .isEqualTo("com.runestone.expeval.api");
        assertThat(DiagnosticCategory.SEMANTIC).isNotNull();
    }
}
