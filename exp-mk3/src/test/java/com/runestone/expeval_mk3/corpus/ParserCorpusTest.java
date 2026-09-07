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
            ExpectedDiagnostic expected = (ExpectedDiagnostic) expressionCase.expectedOutcome();

            ParseResult result = parser.parse(expressionCase.source());

            assertThat(result)
                    .as("%s", expressionCase.path())
                    .isInstanceOf(ParseFailure.class);
            ParseFailure failure = (ParseFailure) result;
            ExpressionDiagnostic primary = failure.diagnostics().getFirst();
            assertThat(primary.category().name())
                    .as("%s", expressionCase.path())
                    .isEqualTo(expected.category());
            assertThat(primary.code())
                    .as("%s", expressionCase.path())
                    .isEqualTo(expected.code());
            assertThat(primary.primarySpan())
                    .as("%s", expressionCase.path())
                    .contains(expected.requiredSpan());
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
        ExpectedDiagnostic expected = (ExpectedDiagnostic) expressionCase.expectedOutcome();
        return DiagnosticCategory.PARSE.name().equals(expected.category());
    }
}
