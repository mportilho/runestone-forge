package com.runestone.expeval_mk3.internal.parser;

import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.api.DiagnosticCategory;
import com.runestone.expeval_mk3.api.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ExpressionParserTest {

    private final ExpressionParser parser = new ExpressionParser();

    @Test
    @DisplayName("valid expression parses through SLL and exposes the ANTLR start tree")
    void validExpressionParsesThroughSll() {
        ParseResult result = parser.parse("1 + 2");

        assertThat(result).isInstanceOf(ParseSuccess.class);
        ParseSuccess success = (ParseSuccess) result;
        assertThat(success.predictionPath()).isEqualTo(PredictionPath.SLL);
        assertThat(success.tree()).isNotNull();
    }

    @Test
    @DisplayName("unrecognized characters become positioned parse diagnostics")
    void unrecognizedCharactersBecomePositionedParseDiagnostics() {
        ParseResult result = parser.parse("a # b");

        assertThat(result).isInstanceOf(ParseFailure.class);
        ParseFailure failure = (ParseFailure) result;
        assertThat(failure.predictionPath()).isEqualTo(PredictionPath.LL_FALLBACK);
        assertThat(failure.diagnostics()).first().satisfies(diagnostic -> {
            assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.PARSE);
            assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.PARSE_UNRECOGNIZED_CHARACTER.name());
            assertThat(diagnostic.primarySpan()).contains(new SourceSpan(2, 3, 1, 3));
        });
    }

    @Test
    @DisplayName("null source is a programming error")
    void nullSourceIsProgrammingError() {
        assertThatNullPointerException()
                .isThrownBy(() -> parser.parse(null))
                .withMessage("source");
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "[null]", "asNumber(null)", "0x10", "077"})
    @DisplayName("obsolete source literals fail before semantic resolution")
    void obsoleteSourceLiteralsFailBeforeSemanticResolution(String source) {
        ParseResult result = parser.parse(source);

        assertThat(result)
                .as(source)
                .isInstanceOf(ParseFailure.class);
    }

    @Test
    @DisplayName("warm-up sources parse through SLL without diagnostics")
    void warmUpSourcesParseThroughSllWithoutDiagnostics() {
        for (String source : ExpressionParser.warmUpSources()) {
            ParseResult result = parser.parse(source);

            assertThat(result)
                    .as(source)
                    .isInstanceOf(ParseSuccess.class);
            assertThat(result.predictionPath())
                    .as(source)
                    .isEqualTo(PredictionPath.SLL);
        }
    }

    @Test
    @DisplayName("warm-up covers representative parser constructs")
    void warmUpCoversRepresentativeParserConstructs() {
        String warmUpCorpus = String.join("\n", ExpressionParser.warmUpSources());

        assertThat(warmUpCorpus).contains("??", "root", "sum(", "?.", "[?(", "d\"", "t\"", "dt\"");
    }

    @Test
    @DisplayName("warm-up fails fast only if a representative source stops parsing in SLL")
    void warmUpParsesRepresentativeSources() {
        parser.warmUp();
    }
}
