package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AstDiagnosticOwnershipTest {

    @Test
    @DisplayName("AST code references the public expression diagnostic contract directly")
    void astCodeReferencesThePublicExpressionDiagnosticContract() {
        assertThat(ExpressionDiagnostic.class.getPackageName())
                .isEqualTo("com.runestone.expeval_mk3.api");
        assertThat(SourceSpan.class.getPackageName())
                .isEqualTo("com.runestone.expeval_mk3.api");
        assertThat(DiagnosticCategory.SEMANTIC).isNotNull();
    }
}
