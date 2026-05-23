package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.ast.ExpressionFileNode;
import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.types.NullType;
import com.runestone.expeval.types.ResolvedType;
import com.runestone.expeval.types.ScalarType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LiteralMaterializerTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 0, 1, 0, 1, 0);

    @Test
    void materializesScalarLiteralValues() {
        assertThat(literalValue("12.30", ScalarType.NUMBER)).isEqualTo(new BigDecimal("12.30"));
        assertThat(literalValue("true", ScalarType.BOOLEAN)).isEqualTo(true);
        assertThat(literalValue("\"hello\\\"world\"", ScalarType.STRING)).isEqualTo("hello\"world");
        assertThat(literalValue("2026-05-23T10:15:30Z", ScalarType.DATETIME))
                .isEqualTo(LocalDateTime.parse("2026-05-23T10:15:30"));
        assertThat(literalValue("null", NullType.INSTANCE)).isNull();
    }

    @Test
    void materializesDynamicInstantLiteralsWithoutResolvedType() {
        ExecutableNode node = LiteralMaterializer.build(literal("currDate"), semanticModel(Map.of()));

        assertThat(node).isEqualTo(new ExecutableDynamicLiteral(DynamicInstant.CURR_DATE));
    }

    private static Object literalValue(String value, ResolvedType resolvedType) {
        NodeId nodeId = new NodeId("literal");
        ExecutableLiteral literal = (ExecutableLiteral) LiteralMaterializer.build(
                new LiteralNode(nodeId, SPAN, value),
                semanticModel(Map.of(nodeId, resolvedType)));
        return literal.precomputed();
    }

    private static LiteralNode literal(String value) {
        return new LiteralNode(new NodeId("literal"), SPAN, value);
    }

    private static SemanticModel semanticModel(Map<NodeId, ResolvedType> resolvedTypes) {
        return new SemanticModel(
                new ExpressionFileNode(new NodeId("file"), SPAN, List.of(), null),
                resolvedTypes,
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                Map.of(),
                List.of());
    }
}
