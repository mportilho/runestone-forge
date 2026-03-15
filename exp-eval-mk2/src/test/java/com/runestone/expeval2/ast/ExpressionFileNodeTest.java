package com.runestone.expeval2.ast;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpressionFileNodeTest {

    @Test
    void shouldStoreAssignmentsAndResultExpressionAsImmutableState() {
        List<AssignmentNode> assignments = new ArrayList<>();
        assignments.add(new SimpleAssignmentNode(nodeId("assign-1"), sourceSpan(), "principal", literalNode("100")));

        ExpressionFileNode fileNode = new ExpressionFileNode(
            nodeId("file-1"),
            sourceSpan(),
            assignments,
            identifierNode("principal")
        );

        assignments.clear();

        assertThat(fileNode.assignments()).hasSize(1);
        assertThatThrownBy(() -> fileNode.assignments().add(new SimpleAssignmentNode(
            nodeId("assign-2"),
            sourceSpan(),
            "rate",
            literalNode("0.1")
        ))).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldRejectNullRequiredFields() {
        assertThatThrownBy(() -> new ExpressionFileNode(null, sourceSpan(), List.of(), identifierNode("answer")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("nodeId must not be null");

        assertThatThrownBy(() -> new ExpressionFileNode(nodeId("file-1"), null, List.of(), identifierNode("answer")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("sourceSpan must not be null");

        assertThatThrownBy(() -> new ExpressionFileNode(nodeId("file-1"), sourceSpan(), null, identifierNode("answer")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("assignments must not be null");

        assertThatThrownBy(() -> new ExpressionFileNode(nodeId("file-1"), sourceSpan(), List.of(), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("resultExpression must not be null");
    }

    private static NodeId nodeId(String value) {
        return new NodeId(value);
    }

    private static SourceSpan sourceSpan() {
        return new SourceSpan(0, 1, 1, 0, 1, 1);
    }

    private static LiteralNode literalNode(String value) {
        return new LiteralNode(nodeId("literal-" + value), sourceSpan(), value);
    }

    private static IdentifierNode identifierNode(String value) {
        return new IdentifierNode(nodeId("identifier-" + value), sourceSpan(), value);
    }
}
