package com.runestone.expeval2.internal.ast;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssignmentNodesTest {

    @Test
    void shouldRejectInvalidSimpleAssignmentArguments() {
        assertThatThrownBy(() -> new SimpleAssignmentNode(null, sourceSpan(), "principal", literalNode("100")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("nodeId must not be null");

        assertThatThrownBy(() -> new SimpleAssignmentNode(nodeId("assign-1"), null, "principal", literalNode("100")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("sourceSpan must not be null");

        assertThatThrownBy(() -> new SimpleAssignmentNode(nodeId("assign-1"), sourceSpan(), "   ", literalNode("100")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("targetName must not be blank");

        assertThatThrownBy(() -> new SimpleAssignmentNode(nodeId("assign-1"), sourceSpan(), "principal", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("value must not be null");
    }

    @Test
    void shouldRejectInvalidDestructuringAssignmentArguments() {
        assertThatThrownBy(() -> new DestructuringAssignmentNode(null, sourceSpan(), List.of("left"), identifierNode("coords")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("nodeId must not be null");

        assertThatThrownBy(() -> new DestructuringAssignmentNode(nodeId("assign-1"), null, List.of("left"), identifierNode("coords")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("sourceSpan must not be null");

        assertThatThrownBy(() -> new DestructuringAssignmentNode(nodeId("assign-1"), sourceSpan(), List.of(), identifierNode("coords")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("targetNames must not be empty");

        assertThatThrownBy(() -> new DestructuringAssignmentNode(nodeId("assign-1"), sourceSpan(), List.of("left", " "), identifierNode("coords")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("targetNames must not contain blank values");

        assertThatThrownBy(() -> new DestructuringAssignmentNode(nodeId("assign-1"), sourceSpan(), List.of("left"), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("value must not be null");
    }

    @Test
    void shouldDefensivelyCopyDestructuringTargets() {
        List<String> targetNames = new ArrayList<>(List.of("left", "right"));

        DestructuringAssignmentNode node = new DestructuringAssignmentNode(
            nodeId("assign-1"),
            sourceSpan(),
            targetNames,
            identifierNode("coords")
        );

        targetNames.clear();

        assertThat(node.targetNames()).containsExactly("left", "right");
        assertThatThrownBy(() -> node.targetNames().add("extra"))
            .isInstanceOf(UnsupportedOperationException.class);
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
