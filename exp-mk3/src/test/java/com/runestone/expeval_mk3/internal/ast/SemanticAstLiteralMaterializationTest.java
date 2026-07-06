package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticAstLiteralMaterializationTest {

    private final ExpressionParser parser = new ExpressionParser();
    private final SemanticAstBuilder astBuilder = new SemanticAstBuilder();

    @Test
    @DisplayName("literal nodes carry materialized typed values instead of raw lexemes")
    void literalNodesCarryMaterializedTypedValuesInsteadOfRawLexemes() {
        ExpressionFileNode ast = build("n := null; b := false; s := \"a\\nb\\\\c\"; i := 9223372036854775808; "
                + "d := 1.230; day := d\"2024-02-29\"; hour := t\"10:30:45\"; "
                + "local := dt\"2024-02-29T10:30:45\"; offset := dt\"2024-02-29T10:30:45-03:00\"; offset");

        assertThat(literalValue(ast.assignments().get(0))).isEqualTo(new NullLiteralValue());
        assertThat(literalValue(ast.assignments().get(1))).isEqualTo(new BooleanLiteralValue(false));
        assertThat(literalValue(ast.assignments().get(2))).isEqualTo(new StringLiteralValue("a\nb\\c"));
        assertThat(literalValue(ast.assignments().get(3)))
                .isEqualTo(new BigIntegerLiteralValue(new BigInteger("9223372036854775808")));
        assertThat(literalValue(ast.assignments().get(4))).isEqualTo(new DecimalLiteralValue(new BigDecimal("1.230")));
        assertThat(literalValue(ast.assignments().get(5))).isEqualTo(new DateLiteralValue(LocalDate.of(2024, 2, 29)));
        assertThat(literalValue(ast.assignments().get(6))).isEqualTo(new TimeLiteralValue(LocalTime.of(10, 30, 45)));
        assertThat(literalValue(ast.assignments().get(7)))
                .isEqualTo(new LocalDateTimeLiteralValue(LocalDateTime.of(2024, 2, 29, 10, 30, 45)));
        assertThat(literalValue(ast.assignments().get(8)))
                .isEqualTo(new OffsetDateTimeLiteralValue(OffsetDateTime.parse("2024-02-29T10:30:45-03:00")));
    }

    @Test
    @DisplayName("current temporal values build dynamic nodes rather than literals")
    void currentTemporalValuesBuildDynamicNodesRatherThanLiterals() {
        ExpressionFileNode ast = build("day := currDate; hour := currTime; instant := currDateTime; instant");

        assertThat(ast.assignments().get(0).expression())
                .isEqualTo(new CurrentTemporalValueNode(
                        ast.assignments().get(0).expression().id(),
                        ast.assignments().get(0).expression().sourceSpan(),
                        CurrentTemporalValueKind.DATE));
        assertThat(ast.assignments().get(1).expression())
                .isEqualTo(new CurrentTemporalValueNode(
                        ast.assignments().get(1).expression().id(),
                        ast.assignments().get(1).expression().sourceSpan(),
                        CurrentTemporalValueKind.TIME));
        assertThat(ast.assignments().get(2).expression())
                .isEqualTo(new CurrentTemporalValueNode(
                        ast.assignments().get(2).expression().id(),
                        ast.assignments().get(2).expression().sourceSpan(),
                        CurrentTemporalValueKind.DATETIME));
        assertThat(AstPrettyPrinter.print(ast))
                .isEqualTo("day := currDate;\nhour := currTime;\ninstant := currDateTime;\ninstant");
    }

    @Test
    @DisplayName("invalid local literal materialization returns stable diagnostics")
    void invalidLocalLiteralMaterializationReturnsStableDiagnostics() {
        SemanticAstResult result = buildResult(
                "bad := d\"2024-02-31\"; worse := dt\"2024-02-31T10:30:45\"; worse");

        assertThat(result).isInstanceOf(SemanticAstFailure.class);
        SemanticAstFailure failure = (SemanticAstFailure) result;
        assertThat(failure.diagnostics()).hasSize(2);
        assertThat(failure.diagnostics().get(0)).satisfies(diagnostic -> {
            assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.SEMANTIC);
            assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.AST_INVALID_DATE_LITERAL);
            assertThat(diagnostic.span()).isEqualTo(new SourceSpan(7, 20, 1, 8));
        });
        assertThat(failure.diagnostics().get(1)).satisfies(diagnostic -> {
            assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.SEMANTIC);
            assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.AST_INVALID_DATETIME_LITERAL);
            assertThat(diagnostic.span()).isEqualTo(new SourceSpan(31, 54, 1, 32));
        });
    }

    @Test
    @DisplayName("canonical pretty-printing preserves string escaping and structural equality")
    void canonicalPrettyPrintingPreservesStringEscapingAndStructuralEquality() {
        ExpressionFileNode original = build("text := \"tab\\tquote\\\"slash\\\\line\\n\"; text");

        String printed = AstPrettyPrinter.print(original);
        ExpressionFileNode reparsed = build(printed);

        assertThat(printed).isEqualTo("text := \"tab\\tquote\\\"slash\\\\line\\n\";\ntext");
        assertThat(AstStructuralEquality.equals(original, reparsed)).isTrue();
    }

    @Test
    @DisplayName("literal materialization is independent from the default time zone")
    void literalMaterializationIsIndependentFromDefaultTimeZone() {
        TimeZone previous = TimeZone.getDefault();
        try {
            String source = "instant := dt\"2024-02-29T10:30:45+02:00\"; instant";
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"));
            ExpressionFileNode kiritimati = build(source);
            String printed = AstPrettyPrinter.print(kiritimati);
            TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
            ExpressionFileNode saoPaulo = build(source);
            ExpressionFileNode reparsed = build(printed);

            assertThat(printed).isEqualTo(source.replace("; ", ";\n"));
            assertThat(AstStructuralEquality.equals(kiritimati, saoPaulo)).isTrue();
            assertThat(AstStructuralEquality.equals(kiritimati, reparsed)).isTrue();
        } finally {
            TimeZone.setDefault(previous);
        }
    }

    private ExpressionFileNode build(String source) {
        SemanticAstResult result = buildResult(source);
        assertThat(result).isInstanceOf(SemanticAstSuccess.class);
        return ((SemanticAstSuccess) result).file();
    }

    private SemanticAstResult buildResult(String source) {
        ParseResult parseResult = parser.parse(source);
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);
        return astBuilder.build((ParseSuccess) parseResult);
    }

    private static LiteralValue literalValue(AssignmentNode assignment) {
        assertThat(assignment.expression()).isInstanceOf(LiteralNode.class);
        return ((LiteralNode) assignment.expression()).value();
    }
}
