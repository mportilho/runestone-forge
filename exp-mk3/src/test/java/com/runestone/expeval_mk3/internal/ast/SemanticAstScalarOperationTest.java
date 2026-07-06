package com.runestone.expeval_mk3.internal.ast;

import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.source.SourceSpan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticAstScalarOperationTest {

    private final ExpressionParser parser = new ExpressionParser();
    private final SemanticAstBuilder astBuilder = new SemanticAstBuilder();

    @Test
    @DisplayName("explicit source grouping builds grouped nodes and survives round-trip")
    void explicitSourceGroupingBuildsGroupedNodesAndSurvivesRoundTrip() {
        ExpressionFileNode ast = build("(1 + 2) * 3");

        BinaryOperationNode multiply = resultAs(ast, BinaryOperationNode.class);

        assertThat(multiply.operator()).isEqualTo(BinaryOperator.MULTIPLY);
        assertThat(multiply.left()).isInstanceOf(GroupedExpressionNode.class);
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("(1 + 2) * 3");
        assertRoundTrips(ast);
    }

    @Test
    @DisplayName("unary logical-not synonyms are canonicalized and unary operators stay nested")
    void unaryLogicalNotSynonymsAreCanonicalizedAndUnaryOperatorsStayNested() {
        ExpressionFileNode ast = build("~\u00AC!flag");

        UnaryOperationNode first = resultAs(ast, UnaryOperationNode.class);
        UnaryOperationNode second = as(first.operand(), UnaryOperationNode.class);
        UnaryOperationNode third = as(second.operand(), UnaryOperationNode.class);

        assertThat(first.operator()).isEqualTo(UnaryOperator.LOGICAL_NOT);
        assertThat(second.operator()).isEqualTo(UnaryOperator.LOGICAL_NOT);
        assertThat(third.operator()).isEqualTo(UnaryOperator.LOGICAL_NOT);
        assertThat(third.operand()).isInstanceOf(IdentifierNode.class);
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("!!!flag");
        assertRoundTrips(ast);
    }

    @Test
    @DisplayName("binary operators preserve grammar associativity including right-associative exponentiation")
    void binaryOperatorsPreserveGrammarAssociativityIncludingRightAssociativeExponentiation() {
        ExpressionFileNode additiveAst = build("1 - 2 - 3");
        ExpressionFileNode exponentAst = build("2 ^ 3 ^ 4");

        BinaryOperationNode subtract = resultAs(additiveAst, BinaryOperationNode.class);
        BinaryOperationNode leftSubtract = as(subtract.left(), BinaryOperationNode.class);
        BinaryOperationNode exponent = resultAs(exponentAst, BinaryOperationNode.class);
        BinaryOperationNode rightExponent = as(exponent.right(), BinaryOperationNode.class);

        assertThat(subtract.operator()).isEqualTo(BinaryOperator.SUBTRACT);
        assertThat(leftSubtract.operator()).isEqualTo(BinaryOperator.SUBTRACT);
        assertThat(exponent.operator()).isEqualTo(BinaryOperator.EXPONENTIATE);
        assertThat(rightExponent.operator()).isEqualTo(BinaryOperator.EXPONENTIATE);
        assertThat(AstPrettyPrinter.print(additiveAst)).isEqualTo("1 - 2 - 3");
        assertThat(AstPrettyPrinter.print(exponentAst)).isEqualTo("2 ^ 3 ^ 4");
        assertRoundTrips(additiveAst);
        assertRoundTrips(exponentAst);
    }

    @Test
    @DisplayName("postfix operator chains build one ordered node with per-operator spans")
    void postfixOperatorChainsBuildOneOrderedNodeWithPerOperatorSpans() {
        ExpressionFileNode ast = build("value%!%");

        PostfixOperationNode postfix = resultAs(ast, PostfixOperationNode.class);

        assertThat(postfix.operand()).isInstanceOf(IdentifierNode.class);
        assertThat(postfix.operators()).containsExactly(
                new PostfixOperatorOccurrence(PostfixOperator.PERCENT, new SourceSpan(5, 6, 1, 6)),
                new PostfixOperatorOccurrence(PostfixOperator.FACTORIAL, new SourceSpan(6, 7, 1, 7)),
                new PostfixOperatorOccurrence(PostfixOperator.PERCENT, new SourceSpan(7, 8, 1, 8)));
        assertThat(AstPrettyPrinter.print(ast)).isEqualTo("value%!%");
        assertRoundTrips(ast);
    }

    @Test
    @DisplayName("between and not between build source-faithful nodes with negation metadata")
    void betweenAndNotBetweenBuildSourceFaithfulNodesWithNegationMetadata() {
        ExpressionFileNode betweenAst = build("score between min and max");
        ExpressionFileNode notBetweenAst = build("score not between min and max");

        BetweenNode between = resultAs(betweenAst, BetweenNode.class);
        BetweenNode notBetween = resultAs(notBetweenAst, BetweenNode.class);

        assertThat(between.negated()).isFalse();
        assertThat(notBetween.negated()).isTrue();
        assertThat(between.value()).isInstanceOf(IdentifierNode.class);
        assertThat(between.lowerBound()).isInstanceOf(IdentifierNode.class);
        assertThat(between.upperBound()).isInstanceOf(IdentifierNode.class);
        assertThat(AstPrettyPrinter.print(betweenAst)).isEqualTo("score between min and max");
        assertThat(AstPrettyPrinter.print(notBetweenAst)).isEqualTo("score not between min and max");
        assertRoundTrips(betweenAst);
        assertRoundTrips(notBetweenAst);
    }

    @Test
    @DisplayName("membership synonyms share one node shape and canonical pretty-printing")
    void membershipSynonymsShareOneNodeShapeAndCanonicalPrettyPrinting() {
        ExpressionFileNode inAst = build("item in values");
        ExpressionFileNode notInAst = build("item not in values");
        ExpressionFileNode ninAst = build("item nin values");

        MembershipNode in = resultAs(inAst, MembershipNode.class);
        MembershipNode notIn = resultAs(notInAst, MembershipNode.class);
        MembershipNode nin = resultAs(ninAst, MembershipNode.class);

        assertThat(in.negated()).isFalse();
        assertThat(notIn.negated()).isTrue();
        assertThat(nin.negated()).isTrue();
        assertThat(AstStructuralEquality.equals(notInAst, ninAst)).isTrue();
        assertThat(AstPrettyPrinter.print(inAst)).isEqualTo("item in values");
        assertThat(AstPrettyPrinter.print(notInAst)).isEqualTo("item not in values");
        assertThat(AstPrettyPrinter.print(ninAst)).isEqualTo("item not in values");
        assertRoundTrips(inAst);
        assertRoundTrips(notInAst);
        assertRoundTrips(ninAst);
    }

    @Test
    @DisplayName("direct null-coalescence chains are variadic without flattening grouped chains")
    void directNullCoalescenceChainsAreVariadicWithoutFlatteningGroupedChains() {
        ExpressionFileNode directAst = build("a ?? b ?? c");
        ExpressionFileNode groupedAst = build("(a ?? b) ?? c");

        NullCoalescenceNode direct = resultAs(directAst, NullCoalescenceNode.class);
        NullCoalescenceNode grouped = resultAs(groupedAst, NullCoalescenceNode.class);

        assertThat(direct.operands()).hasSize(3);
        assertThat(grouped.operands()).hasSize(2);
        assertThat(grouped.operands().getFirst()).isInstanceOf(GroupedExpressionNode.class);
        assertThat(AstPrettyPrinter.print(directAst)).isEqualTo("a ?? b ?? c");
        assertThat(AstPrettyPrinter.print(groupedAst)).isEqualTo("(a ?? b) ?? c");
        assertRoundTrips(directAst);
        assertRoundTrips(groupedAst);
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

    private static <T extends ExpressionNode> T as(ExpressionNode node, Class<T> type) {
        assertThat(node).isInstanceOf(type);
        return type.cast(node);
    }
}
