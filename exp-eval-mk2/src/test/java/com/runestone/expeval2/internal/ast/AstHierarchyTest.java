package com.runestone.expeval2.internal.ast;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AstHierarchyTest {

    @Test
    void shouldDeclareSealedRootHierarchy() {
        assertThat(Node.class.isSealed()).isTrue();
        assertThat(AssignmentNode.class.isSealed()).isTrue();
        assertThat(ExpressionNode.class.isSealed()).isTrue();
    }

    @Test
    void shouldExposeExpectedDirectSubtypesFromNode() {
        assertThat(permittedSubclassNamesOf(Node.class)).containsExactlyInAnyOrder(
            AssignmentNode.class.getName(),
            ExpressionFileNode.class.getName(),
            ExpressionNode.class.getName()
        );
    }

    @Test
    void shouldExposeExpectedDirectSubtypesFromAssignmentNode() {
        assertThat(permittedSubclassNamesOf(AssignmentNode.class)).containsExactlyInAnyOrder(
            DestructuringAssignmentNode.class.getName(),
            SimpleAssignmentNode.class.getName()
        );
    }

    @Test
    void shouldExposeExpectedDirectSubtypesFromExpressionNode() {
        assertThat(permittedSubclassNamesOf(ExpressionNode.class)).containsExactlyInAnyOrder(
            BinaryOperationNode.class.getName(),
            ConditionalNode.class.getName(),
            FunctionCallNode.class.getName(),
            IdentifierNode.class.getName(),
            LiteralNode.class.getName(),
            PostfixOperationNode.class.getName(),
            UnaryOperationNode.class.getName(),
            VectorLiteralNode.class.getName()
        );
    }

    private static Set<String> permittedSubclassNamesOf(Class<?> type) {
        return Arrays.stream(type.getPermittedSubclasses())
            .map(Class::getName)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
}
