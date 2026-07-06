package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SemanticAstConditionalAndVectorTest {

    private final ExpressionParser parser = new ExpressionParser();
    private final SemanticAstBuilder astBuilder = new SemanticAstBuilder();

    @Test
    @DisplayName("classic and functional conditionals share one node shape with source-form metadata")
    void classicAndFunctionalConditionalsShareOneNodeShapeWithSourceFormMetadata() {
        ExpressionFileNode classicAst = build("if ready then 1 elsif backup then 2 else 3 endif");
        ExpressionFileNode functionalAst = build("if(ready, 1, backup, 2, 3)");

        ConditionalNode classic = resultAs(classicAst, ConditionalNode.class);
        ConditionalNode functional = resultAs(functionalAst, ConditionalNode.class);

        assertThat(classic.sourceForm()).isEqualTo(ConditionalSourceForm.CLASSIC);
        assertThat(functional.sourceForm()).isEqualTo(ConditionalSourceForm.FUNCTIONAL);
        assertThat(classic.branches()).hasSize(2);
        assertThat(functional.branches()).hasSize(2);
        assertThat(classic.branches()).allSatisfy(branch -> {
            assertThat(branch.id()).isNotEqualTo(NodeId.UNASSIGNED);
            assertThat(branch.sourceSpan().endOffset()).isGreaterThan(branch.sourceSpan().offset());
        });
        assertThat(classic.elseExpression()).isInstanceOf(LiteralNode.class);
        assertThat(functional.elseExpression()).isInstanceOf(LiteralNode.class);
        assertThat(AstPrettyPrinter.print(classicAst)).isEqualTo("if ready then 1 elsif backup then 2 else 3 endif");
        assertThat(AstPrettyPrinter.print(functionalAst)).isEqualTo("if(ready, 1, backup, 2, 3)");
        assertRoundTrips(classicAst);
        assertRoundTrips(functionalAst);
    }

    @Test
    @DisplayName("conditional branches and else expression carry source spans suitable for diagnostics")
    void conditionalBranchesAndElseExpressionCarrySourceSpansSuitableForDiagnostics() {
        ExpressionFileNode ast = build("if ready then 1 else fallback endif");

        ConditionalNode conditional = resultAs(ast, ConditionalNode.class);
        ConditionalBranchNode branch = conditional.branches().getFirst();

        assertThat(conditional.sourceSpan()).isEqualTo(new SourceSpan(0, 35, 1, 1));
        assertThat(branch.sourceSpan()).isEqualTo(new SourceSpan(0, 15, 1, 1));
        assertThat(branch.condition().sourceSpan()).isEqualTo(new SourceSpan(3, 8, 1, 4));
        assertThat(branch.resultExpression().sourceSpan()).isEqualTo(new SourceSpan(14, 15, 1, 15));
        assertThat(conditional.elseExpression().id()).isNotEqualTo(NodeId.UNASSIGNED);
        assertThat(conditional.elseExpression().sourceSpan()).isEqualTo(new SourceSpan(21, 29, 1, 22));
    }

    @Test
    @DisplayName("vector literals build immutable nodes spanning the bracketed source")
    void vectorLiteralsBuildImmutableNodesSpanningTheBracketedSource() {
        ExpressionFileNode ast = build("[1, value, if ok then 2 else 3 endif]");

        VectorLiteralNode vector = resultAs(ast, VectorLiteralNode.class);

        assertThat(vector.sourceSpan()).isEqualTo(new SourceSpan(0, 37, 1, 1));
        assertThat(vector.elements()).hasSize(3);
        assertThat(vector.elements().get(0)).isInstanceOf(LiteralNode.class);
        assertThat(vector.elements().get(1)).isInstanceOf(IdentifierNode.class);
        assertThat(vector.elements().get(2)).isInstanceOf(ConditionalNode.class);
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> vector.elements().add(vector.elements().getFirst()));
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("[1, value, if ok then 2 else 3 endif]");
        assertRoundTrips(ast);
    }

    @Test
    @DisplayName("empty vector literals are distinct nodes that reuse the immutable empty element list")
    void emptyVectorLiteralsAreDistinctNodesThatReuseTheImmutableEmptyElementList() {
        ExpressionFileNode ast = build("first := []; second := []; second");

        VectorLiteralNode first = assignmentExpressionAs(ast, 0, VectorLiteralNode.class);
        VectorLiteralNode second = assignmentExpressionAs(ast, 1, VectorLiteralNode.class);

        assertThat(first).isNotSameAs(second);
        assertThat(first.id()).isNotEqualTo(second.id());
        assertThat(first.sourceSpan()).isEqualTo(new SourceSpan(9, 11, 1, 10));
        assertThat(second.sourceSpan()).isEqualTo(new SourceSpan(23, 25, 1, 24));
        assertThat(first.elements()).isSameAs(second.elements()).isEmpty();
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("first := [];\nsecond := [];\nsecond");
        assertRoundTrips(ast);
    }

    private ExpressionFileNode build(String source) {
        ParseResult parseResult = parser.parse(source);
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);
        SemanticAstResult result = astBuilder.build((ParseSuccess) parseResult);
        assertThat(result).isInstanceOf(SemanticAstSuccess.class);
        return ((SemanticAstSuccess) result).file();
    }

    private void assertRoundTrips(ExpressionFileNode ast) {
        ExpressionFileNode reparsed = build(AstPrettyPrinter.print(ast));

        assertThat(AstStructuralEquality.equals(ast, reparsed)).isTrue();
    }

    private static <T extends ExpressionNode> T resultAs(ExpressionFileNode ast, Class<T> type) {
        assertThat(ast.resultExpression()).isPresent();
        return as(ast.resultExpression().orElseThrow(), type);
    }

    private static <T extends ExpressionNode> T assignmentExpressionAs(
            ExpressionFileNode ast,
            int assignmentIndex,
            Class<T> type) {
        return as(ast.assignments().get(assignmentIndex).expression(), type);
    }

    private static <T extends ExpressionNode> T as(ExpressionNode node, Class<T> type) {
        assertThat(node).isInstanceOf(type);
        return type.cast(node);
    }
}
