package com.runestone.expeval2.internal.ast;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionNodesTest {

    @Test
    void shouldRejectBlankIdentifierName() {
        assertThatThrownBy(() -> new IdentifierNode(nodeId("identifier-1"), sourceSpan(), "  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("name must not be blank");
    }

    @Test
    void shouldRejectNullLiteralValue() {
        assertThatThrownBy(() -> new LiteralNode(nodeId("literal-1"), sourceSpan(), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("value must not be null");
    }

    @Test
    void shouldRejectInvalidFunctionCallArguments() {
        assertThatThrownBy(() -> new FunctionCallNode(nodeId("call-1"), sourceSpan(), "  ", List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("functionName must not be blank");

        assertThatThrownBy(() -> new FunctionCallNode(nodeId("call-1"), sourceSpan(), "score", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("arguments must not be null");
    }

    @Test
    void shouldDefensivelyCopyFunctionArguments() {
        List<ExpressionNode> arguments = new ArrayList<>(List.of(identifierNode("principal")));

        FunctionCallNode node = new FunctionCallNode(nodeId("call-1"), sourceSpan(), "score", arguments);

        arguments.clear();

        assertThat(node.arguments()).hasSize(1);
        assertThatThrownBy(() -> node.arguments().add(identifierNode("rate")))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldRejectInvalidConditionalArguments() {
        assertThatThrownBy(() -> new ConditionalNode(nodeId("cond-1"), sourceSpan(), List.of(), List.of(), literalNode("fallback")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("conditions must not be empty");

        assertThatThrownBy(() -> new ConditionalNode(
            nodeId("cond-1"),
            sourceSpan(),
            List.of(identifierNode("condition")),
            List.of(literalNode("first"), literalNode("second")),
            literalNode("fallback")
        )).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("conditions and results must have the same size");
    }

    @Test
    void shouldRejectInvalidOperationArguments() {
        assertThatThrownBy(() -> new UnaryOperationNode(nodeId("unary-1"), sourceSpan(), null, identifierNode("value")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("operator must not be null");

        assertThatThrownBy(() -> new BinaryOperationNode(nodeId("binary-1"), sourceSpan(), BinaryOperator.ADD, null, literalNode("1")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("left must not be null");

        assertThatThrownBy(() -> new PostfixOperationNode(nodeId("postfix-1"), sourceSpan(), PostfixOperator.PERCENT, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("operand must not be null");
    }

    @Test
    void shouldPreserveSemanticOperatorEnums() {
        UnaryOperationNode unaryNode = new UnaryOperationNode(
            nodeId("unary-1"),
            sourceSpan(),
            UnaryOperator.SQRT,
            identifierNode("value")
        );
        BinaryOperationNode binaryNode = new BinaryOperationNode(
            nodeId("binary-1"),
            sourceSpan(),
            BinaryOperator.NOT_EQUAL,
            identifierNode("left"),
            identifierNode("right")
        );
        PostfixOperationNode postfixNode = new PostfixOperationNode(
            nodeId("postfix-1"),
            sourceSpan(),
            PostfixOperator.FACTORIAL,
            literalNode("10")
        );

        assertThat(unaryNode.operator()).isEqualTo(UnaryOperator.SQRT);
        assertThat(binaryNode.operator()).isEqualTo(BinaryOperator.NOT_EQUAL);
        assertThat(postfixNode.operator()).isEqualTo(PostfixOperator.FACTORIAL);
    }

    @Test
    void shouldDefensivelyCopyVectorElements() {
        List<ExpressionNode> elements = new ArrayList<>(List.of(literalNode("1")));

        VectorLiteralNode node = new VectorLiteralNode(nodeId("vector-1"), sourceSpan(), elements);

        elements.clear();

        assertThat(node.elements()).hasSize(1);
        assertThatThrownBy(() -> node.elements().add(literalNode("2")))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    private static NodeId nodeId(String value) {
        return new NodeId(value);
    }

    private static SourceSpan sourceSpan() {
        return new SourceSpan(0, 1, 1, 0, 1, 1);
    }

    private static IdentifierNode identifierNode(String value) {
        return new IdentifierNode(nodeId("identifier-" + value), sourceSpan(), value);
    }

    private static LiteralNode literalNode(String value) {
        return new LiteralNode(nodeId("literal-" + value), sourceSpan(), value);
    }
}
