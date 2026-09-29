package com.runestone.expeval.internal.ast;

import com.runestone.expeval.internal.parser.ExpressionParser;
import com.runestone.expeval.internal.parser.ParseResult;
import com.runestone.expeval.internal.parser.ParseSuccess;
import com.runestone.expeval.support.DocumentationPaths;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

final class DocumentedPrecedenceTest {

    private final ExpressionParser parser = new ExpressionParser();
    private final SemanticAstBuilder astBuilder = new SemanticAstBuilder();

    @Test
    void everyDocumentedPrecedenceRowHasAnAstShapeTest() throws IOException {
        Map<String, String> documentedCases = documentedCases();
        Map<String, Consumer<ExpressionNode>> assertions = new LinkedHashMap<>();
        assertions.put("PREC-01", this::assertOrderedNullCoalescence);
        assertions.put("PREC-02", leftAssociative(BinaryOperator.LOGICAL_OR));
        assertions.put("PREC-03", leftAssociative(BinaryOperator.LOGICAL_AND));
        assertions.put("PREC-04", this::assertComparisonPrecedence);
        assertions.put("PREC-05", leftAssociative(BinaryOperator.LOGICAL_XOR));
        assertions.put("PREC-06", leftAssociative(BinaryOperator.CONCATENATE));
        assertions.put("PREC-07", leftAssociative(BinaryOperator.SUBTRACT));
        assertions.put("PREC-08", leftAssociative(BinaryOperator.DIVIDE));
        assertions.put("PREC-09", this::assertUnaryPrecedence);
        assertions.put("PREC-10", leftAssociative(BinaryOperator.ROOT));
        assertions.put("PREC-11", this::assertRightAssociativeExponentiation);
        assertions.put("PREC-12", this::assertPostfixSourceOrder);
        assertions.put("PREC-13", node -> assertThat(node).isInstanceOf(GroupedExpressionNode.class));

        assertThat(documentedCases.keySet()).containsExactlyElementsOf(assertions.keySet());
        documentedCases.forEach((id, source) -> assertions.get(id).accept(build(source)));
    }

    private static Consumer<ExpressionNode> binary(BinaryOperator expected) {
        return node -> {
            assertThat(node).isInstanceOf(BinaryOperationNode.class);
            assertThat(((BinaryOperationNode) node).operator()).isEqualTo(expected);
        };
    }

    private static Consumer<ExpressionNode> unary(UnaryOperator expected) {
        return node -> {
            assertThat(node).isInstanceOf(UnaryOperationNode.class);
            assertThat(((UnaryOperationNode) node).operator()).isEqualTo(expected);
        };
    }

    private static Consumer<ExpressionNode> leftAssociative(BinaryOperator expected) {
        return node -> {
            binary(expected).accept(node);
            assertThat(((BinaryOperationNode) node).left()).isInstanceOfSatisfying(
                    BinaryOperationNode.class,
                    left -> assertThat(left.operator()).isEqualTo(expected));
        };
    }

    private void assertOrderedNullCoalescence(ExpressionNode node) {
        assertThat(node).isInstanceOfSatisfying(NullCoalesceNode.class, coalesce ->
                assertThat(coalesce.operands())
                        .extracting(operand -> ((IdentifierNode) operand).name())
                        .containsExactly("a", "b", "c"));
    }

    private void assertComparisonPrecedence(ExpressionNode node) {
        binary(BinaryOperator.EQUAL).accept(node);
        assertThat(((BinaryOperationNode) node).right()).isInstanceOfSatisfying(
                BinaryOperationNode.class,
                right -> assertThat(right.operator()).isEqualTo(BinaryOperator.LOGICAL_XOR));
    }

    private void assertUnaryPrecedence(ExpressionNode node) {
        unary(UnaryOperator.NEGATE).accept(node);
        assertThat(((UnaryOperationNode) node).operand()).isInstanceOfSatisfying(
                BinaryOperationNode.class,
                operand -> assertThat(operand.operator()).isEqualTo(BinaryOperator.ROOT));
    }

    private void assertRightAssociativeExponentiation(ExpressionNode node) {
        binary(BinaryOperator.EXPONENTIATE).accept(node);
        assertThat(((BinaryOperationNode) node).right()).isInstanceOfSatisfying(
                BinaryOperationNode.class,
                right -> assertThat(right.operator()).isEqualTo(BinaryOperator.EXPONENTIATE));
    }

    private void assertPostfixSourceOrder(ExpressionNode node) {
        assertThat(node).isInstanceOfSatisfying(PostfixOperationNode.class, postfix ->
                assertThat(postfix.operations()).extracting(PostfixOperatorOccurrence::operator)
                        .containsExactly(PostfixOperator.PERCENT, PostfixOperator.FACTORIAL));
    }

    private ExpressionNode build(String source) {
        ParseResult parseResult = parser.parse(source);
        assertThat(parseResult).isInstanceOf(ParseSuccess.class);
        SemanticAstBuildResult result = astBuilder.build((ParseSuccess) parseResult);
        assertThat(result).isInstanceOf(SemanticAstBuildSuccess.class);
        return ((SemanticAstBuildSuccess) result).file().resultExpression().orElseThrow();
    }

    private static Map<String, String> documentedCases() throws IOException {
        Path reference = DocumentationPaths.moduleRoot().resolve("docs/reference/language.md");
        Map<String, String> cases = new LinkedHashMap<>();
        for (String line : Files.readAllLines(reference)) {
            if (!line.matches("\\| `PREC-[0-9]{2}` \\|.*")) {
                continue;
            }
            List<String> cells = java.util.Arrays.stream(line.substring(1, line.length() - 1).split("\\|"))
                    .map(String::trim)
                    .toList();
            cases.put(
                    cells.getFirst().replace("`", ""),
                    cells.getLast().replace("`", "").replace("&#124;", "|"));
        }
        return cases;
    }

}
