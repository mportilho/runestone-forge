package com.runestone.expeval_mk3.corpus;

import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.ExpressionCompilationResult;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionEngine;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseFailure;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.parser.PredictionPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ParserCorpusTest {

    private final ExpressionParser parser = new ExpressionParser();

    @Test
    @DisplayName("all valid expression cases parse through SLL without fallback")
    void allValidExpressionCasesParseThroughSllWithoutFallback() {
        for (ExpressionCase expressionCase : ExpressionCaseLoader.loadAll()) {
            if (expressionCase.kind() != CaseKind.VALID) {
                continue;
            }

            ParseResult result = parser.parse(expressionCase.source());

            assertThat(result)
                    .as("%s", expressionCase.path())
                    .isInstanceOf(ParseSuccess.class);
            assertThat(result.predictionPath())
                    .as("%s", expressionCase.path())
                    .isEqualTo(PredictionPath.SLL);
        }
    }

    @Test
    @DisplayName("parser PARSE invalid cases expose the expected primary diagnostic")
    void parserParseInvalidCasesExposeExpectedPrimaryDiagnostic() {
        for (ExpressionCase expressionCase : ExpressionCaseLoader.loadAll()) {
            if (!isParserParseInvalidCase(expressionCase)) {
                continue;
            }
            ExpectedDiagnostics expected = (ExpectedDiagnostics) expressionCase.expectedOutcome();

            ParseResult result = parser.parse(expressionCase.source());

            assertThat(result)
                    .as("%s", expressionCase.path())
                    .isInstanceOf(ParseFailure.class);
            ParseFailure failure = (ParseFailure) result;
            assertThat(failure.diagnostics()).as("%s", expressionCase.path())
                    .hasSameSizeAs(expected.diagnostics());
            for (int index = 0; index < expected.diagnostics().size(); index++) {
                ExpressionDiagnostic actual = failure.diagnostics().get(index);
                ExpectedDiagnostic expectedDiagnostic = expected.diagnostics().get(index);
                assertThat(actual.category().name()).as("%s diagnostic %d", expressionCase.path(), index)
                        .isEqualTo(expectedDiagnostic.category());
                assertThat(actual.code()).as("%s diagnostic %d", expressionCase.path(), index)
                        .isEqualTo(expectedDiagnostic.code());
                assertThat(actual.primarySpan()).as("%s diagnostic %d", expressionCase.path(), index)
                        .contains(expectedDiagnostic.requiredSpan());
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"|x|", "x = 2 + 3;", "10:30", "<number>(x)"})
    @DisplayName("unsupported historical syntax receives ordinary language diagnostics")
    void unsupportedHistoricalSyntaxReceivesOrdinaryLanguageDiagnostics(String source) {
        ExpressionCompilationResult result = ExpressionEngine.defaultEngine().compile(
                source, ExpressionEnvironment.builder().build());

        assertThat(result).isInstanceOfSatisfying(ExpressionCompilationResult.Failure.class, failure -> {
            ExpressionDiagnostic primary = failure.diagnostics().getFirst();
            assertThat(primary.category()).isIn(DiagnosticCategory.PARSE, DiagnosticCategory.SEMANTIC);
            assertThat(primary.code()).isNotBlank().isNotEqualTo("TBD");
        });
    }

    private static boolean isParserParseInvalidCase(ExpressionCase expressionCase) {
        if (expressionCase.kind() != CaseKind.INVALID || expressionCase.phase() != CasePhase.PARSER) {
            return false;
        }
        ExpectedDiagnostics expected = (ExpectedDiagnostics) expressionCase.expectedOutcome();
        return expected.diagnostics().stream()
                .allMatch(diagnostic -> DiagnosticCategory.PARSE.name().equals(diagnostic.category()));
    }
}
