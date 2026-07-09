package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCategory;
import com.runestone.expeval_mk3.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SemanticAstPipelineTest {

    private final ExpressionParser parser = new ExpressionParser();
    private final SemanticAstBuilder astBuilder = new SemanticAstBuilder();

    @Test
    @DisplayName("simple source with assignments and result builds a deterministic semantic AST")
    void simpleSourceWithAssignmentsAndResultBuildsDeterministicSemanticAst() {
        String source = "total:=42; label := \"ok\";\nready := true; total";

        ExpressionFileNode ast = build(source);

        assertThat(ast.assignments()).hasSize(3);
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> ast.assignments().add(ast.assignments().getFirst()));
        assertThat(ast.resultExpression()).hasValueSatisfying(result ->
                assertThat(result).isEqualTo(new IdentifierNode(new NodeId(10), result.sourceSpan(), "total")));
        assertThat(nodeIds(ast)).containsExactly(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("total := 42;\nlabel := \"ok\";\nready := true;\ntotal");
    }

    @Test
    @DisplayName("assignments-only source builds an expression file with an empty result expression")
    void assignmentsOnlySourceBuildsExpressionFileWithEmptyResultExpression() {
        ExpressionFileNode ast = build("first := null;\nsecond := first;");
        ExpressionFileNode reparsed = build(AstPrettyPrinter.print(ast));

        assertThat(ast.assignments()).hasSize(2);
        assertThat(ast.resultExpression()).isEmpty();
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("first := null;\nsecond := first;");
        assertThat(reparsed.resultExpression()).isEmpty();
        assertThat(AstStructuralEquality.equals(ast, reparsed)).isTrue();
    }

    @Test
    @DisplayName("literal AST values are materialized independently from the environment")
    void literalAstValuesAreMaterializedIndependentlyFromEnvironment() {
        ExpressionFileNode ast = build("nothing := null; truth := true; text := \"line\\n\\\"quoted\\\"\\\\path\"; "
                + "small := 9223372036854775807; big := 9223372036854775808; "
                + "amount := 001.2300; day := d\"2024-01-02\"; time := t\"10:30\"; "
                + "local := dt\"2024-01-02T10:30:00\"; instant := dt\"2024-01-02T10:30:00+02:00\"; instant");

        assertThat(literalValue(ast.assignments().get(0).expression()))
                .isEqualTo(new NullLiteralValue());
        assertThat(literalValue(ast.assignments().get(1).expression()))
                .isEqualTo(new BooleanLiteralValue(true));
        assertThat(literalValue(ast.assignments().get(2).expression()))
                .isEqualTo(new StringLiteralValue("line\n\"quoted\"\\path"));
        assertThat(literalValue(ast.assignments().get(3).expression()))
                .isEqualTo(new LongLiteralValue(Long.MAX_VALUE));
        assertThat(literalValue(ast.assignments().get(4).expression()))
                .isEqualTo(new BigIntegerLiteralValue(new BigInteger("9223372036854775808")));
        assertThat(literalValue(ast.assignments().get(5).expression()))
                .isEqualTo(new DecimalLiteralValue(new BigDecimal("001.2300")));
        assertThat(literalValue(ast.assignments().get(6).expression()))
                .isEqualTo(new DateLiteralValue(LocalDate.of(2024, 1, 2)));
        assertThat(literalValue(ast.assignments().get(7).expression()))
                .isEqualTo(new TimeLiteralValue(LocalTime.of(10, 30)));
        assertThat(literalValue(ast.assignments().get(8).expression()))
                .isEqualTo(new LocalDateTimeLiteralValue(LocalDateTime.of(2024, 1, 2, 10, 30)));
        assertThat(literalValue(ast.assignments().get(9).expression()))
                .isEqualTo(new OffsetDateTimeLiteralValue(OffsetDateTime.parse("2024-01-02T10:30:00+02:00")));
        assertThat(ast.resultExpression()).hasValueSatisfying(result ->
                assertThat(result).isInstanceOf(IdentifierNode.class));

        String printed = AstPrettyPrinter.print(ast);
        assertThat(printed).contains("text := \"line\\n\\\"quoted\\\"\\\\path\";");
        assertThat(AstStructuralEquality.equals(ast, build(printed))).isTrue();

        ExpressionFileNode utcOffset = build("instant := dt\"2024-01-02T10:30:00+00:00\"; instant");
        String printedOffset = AstPrettyPrinter.print(utcOffset);
        assertThat(printedOffset).contains("+00:00").doesNotContain("Z");
        assertThat(AstStructuralEquality.equals(utcOffset, build(printedOffset))).isTrue();

        ExpressionFileNode plusTwoOffset = build("instant := dt\"2024-01-02T10:30:00+02:00\"; instant");
        ExpressionFileNode sameInstantUtcOffset = build("instant := dt\"2024-01-02T08:30:00+00:00\"; instant");
        assertThat(AstStructuralEquality.equals(plusTwoOffset, sameInstantUtcOffset)).isFalse();

        ExpressionFileNode decimalWithLeadingZero = build("leading := 018; leading");
        assertThat(literalValue(decimalWithLeadingZero.assignments().getFirst().expression()))
                .isEqualTo(new LongLiteralValue(18));
    }

    @Test
    @DisplayName("current temporal values build dynamic expression nodes")
    void currentTemporalValuesBuildDynamicExpressionNodes() {
        ExpressionFileNode ast = build("day := currDate; time := currTime; instant := currDateTime; instant");

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
                        CurrentTemporalValueKind.DATE_TIME));
        assertThat(AstPrettyPrinter.print(ast))
                .isEqualTo("day := currDate;\ntime := currTime;\ninstant := currDateTime;\ninstant");
        assertThat(AstStructuralEquality.equals(ast, build(AstPrettyPrinter.print(ast)))).isTrue();
    }

    @Test
    @DisplayName("invalid local temporal materialization returns stable diagnostics")
    void invalidLocalTemporalMaterializationReturnsStableDiagnostics() {
        ParseResult parseResult = parser.parse("badDate := d\"2024-02-30\"; badDateTime := dt\"2024-02-30T10:15:30\";");
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);

        SemanticAstBuildResult buildResult = astBuilder.build((ParseSuccess) parseResult);

        assertThat(buildResult).isInstanceOf(SemanticAstBuildFailure.class);
        assertThat(((SemanticAstBuildFailure) buildResult).diagnostics()).satisfiesExactly(
                diagnostic -> {
                    assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.SEMANTIC);
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.AST_INVALID_DATE_LITERAL);
                    assertThat(diagnostic.span().offset()).isEqualTo(11);
                    assertThat(diagnostic.span().endOffset()).isEqualTo(24);
                },
                diagnostic -> {
                    assertThat(diagnostic.category()).isEqualTo(DiagnosticCategory.SEMANTIC);
                    assertThat(diagnostic.code()).isEqualTo(DiagnosticCode.AST_INVALID_DATE_TIME_LITERAL);
                    assertThat(diagnostic.span().offset()).isEqualTo(41);
                    assertThat(diagnostic.span().endOffset()).isEqualTo(64);
                });
    }

    @Test
    @DisplayName("pretty-printed tracer AST reparses to a structurally equal tree")
    void prettyPrintedTracerAstReparsesToStructurallyEqualTree() {
        ExpressionFileNode original = build("x:=1; y := x; y");

        ExpressionFileNode reparsed = build(AstPrettyPrinter.print(original));
        ExpressionFileNode reidentified = shiftNodeIds(original, 100);

        assertThat(AstStructuralEquality.equals(original, reparsed)).isTrue();
        assertThat(reidentified).isNotEqualTo(original);
        assertThat(AstStructuralEquality.equals(original, reidentified)).isTrue();
    }

    @Test
    @DisplayName("only the root AST record uses Optional for the result expression")
    void onlyRootAstRecordUsesOptionalForResultExpression() {
        List<RecordComponent> optionalComponents = astRecordTypes().stream()
                .flatMap(type -> List.of(type.getRecordComponents()).stream())
                .filter(component -> component.getType().equals(Optional.class))
                .toList();

        assertThat(optionalComponents).singleElement().satisfies(component -> {
            assertThat(component.getDeclaringRecord()).isEqualTo(ExpressionFileNode.class);
            assertThat(component.getName()).isEqualTo("resultExpression");
        });
    }

    private ExpressionFileNode build(String source) {
        ParseResult parseResult = parser.parse(source);
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);
        SemanticAstBuildResult buildResult = astBuilder.build((ParseSuccess) parseResult);
        assertThat(buildResult).isInstanceOf(SemanticAstBuildSuccess.class);
        return ((SemanticAstBuildSuccess) buildResult).file();
    }

    private static List<Integer> nodeIds(ExpressionFileNode file) {
        List<Integer> ids = new ArrayList<>();
        ids.add(file.id().value());
        for (AssignmentNode assignment : file.assignments()) {
            ids.add(assignment.id().value());
            ids.add(assignment.target().id().value());
            ids.add(assignment.expression().id().value());
        }
        file.resultExpression().ifPresent(expression -> ids.add(expression.id().value()));
        return ids;
    }

    private static List<Class<?>> astRecordTypes() {
        List<Class<?>> recordTypes = new ArrayList<>();
        collectAstRecordTypes(AstNode.class, recordTypes);
        return recordTypes;
    }

    private static void collectAstRecordTypes(Class<?> type, List<Class<?>> recordTypes) {
        if (type.isRecord()) {
            recordTypes.add(type);
        }
        Class<?>[] permittedSubclasses = type.getPermittedSubclasses();
        if (permittedSubclasses == null) {
            return;
        }
        for (Class<?> permittedSubclass : permittedSubclasses) {
            collectAstRecordTypes(permittedSubclass, recordTypes);
        }
    }

    private static LiteralValue literalValue(ExpressionNode expression) {
        assertThat(expression).isInstanceOf(LiteralNode.class);
        return ((LiteralNode) expression).value();
    }

    private static ExpressionFileNode shiftNodeIds(ExpressionFileNode file, int offset) {
        return new ExpressionFileNode(
                shift(file.id(), offset),
                file.sourceSpan(),
                file.assignments().stream().map(assignment -> shiftNodeIds(assignment, offset)).toList(),
                file.resultExpression().map(expression -> shiftNodeIds(expression, offset)));
    }

    private static AssignmentNode shiftNodeIds(AssignmentNode assignment, int offset) {
        return new AssignmentNode(
                shift(assignment.id(), offset),
                assignment.sourceSpan(),
                shiftNodeIds(assignment.target(), offset),
                shiftNodeIds(assignment.expression(), offset));
    }

    private static AssignmentTargetNode shiftNodeIds(AssignmentTargetNode target, int offset) {
        if (target instanceof IdentifierAssignmentTargetNode identifier) {
            return new IdentifierAssignmentTargetNode(
                    shift(identifier.id(), offset),
                    identifier.sourceSpan(),
                    identifier.name());
        }
        throw new IllegalArgumentException("Unsupported target: " + target.getClass().getName());
    }

    private static ExpressionNode shiftNodeIds(ExpressionNode expression, int offset) {
        return switch (expression) {
            case CurrentTemporalValueNode currentTemporalValue -> new CurrentTemporalValueNode(
                    shift(currentTemporalValue.id(), offset),
                    currentTemporalValue.sourceSpan(),
                    currentTemporalValue.kind());
            case IdentifierNode identifier -> new IdentifierNode(
                    shift(identifier.id(), offset),
                    identifier.sourceSpan(),
                    identifier.name());
            case LiteralNode literal -> new LiteralNode(shift(literal.id(), offset), literal.sourceSpan(), literal.value());
        };
    }

    private static NodeId shift(NodeId id, int offset) {
        return new NodeId(id.value() + offset);
    }
}
