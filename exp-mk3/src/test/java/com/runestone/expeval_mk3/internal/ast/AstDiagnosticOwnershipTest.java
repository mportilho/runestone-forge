package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.ExpressionDiagnostic;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AstDiagnosticOwnershipTest {

    @Test
    @DisplayName("AST code can reference expression diagnostics without parser ownership")
    void astCodeCanReferenceExpressionDiagnosticsWithoutParserOwnership() {
        assertThat(ExpressionDiagnostic.class.getPackageName())
                .isEqualTo("com.runestone.expeval_mk3.internal.diagnostics");
        assertThat(SourceSpan.class.getPackageName())
                .isEqualTo("com.runestone.expeval_mk3.internal.source");
        assertThat(DiagnosticCategory.SEMANTIC).isNotNull();
    }
}
