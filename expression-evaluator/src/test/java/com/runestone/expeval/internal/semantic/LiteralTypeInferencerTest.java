package com.runestone.expeval.internal.semantic;

import com.runestone.expeval.internal.ast.LiteralNode;
import com.runestone.expeval.internal.ast.NodeId;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.types.NullType;
import com.runestone.expeval.types.ScalarType;
import com.runestone.expeval.types.UnknownType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LiteralTypeInferencerTest {

    private static final SourceSpan SPAN = new SourceSpan(0, 1, 1, 0, 1, 1);

    @Test
    void shouldClassifyQuotedStringBeforeTemporalParsing() {
        assertThat(infer("\"2026-05-23\"")).isEqualTo(ScalarType.STRING);
        assertThat(infer("\"currDate\"")).isEqualTo(ScalarType.STRING);
    }

    @Test
    void shouldClassifyScalarLiteralTypes() {
        assertThat(infer("null")).isEqualTo(NullType.INSTANCE);
        assertThat(infer("true")).isEqualTo(ScalarType.BOOLEAN);
        assertThat(infer("false")).isEqualTo(ScalarType.BOOLEAN);
        assertThat(infer("10.25")).isEqualTo(ScalarType.NUMBER);
    }

    @Test
    void shouldClassifyTemporalLiteralTypes() {
        assertThat(infer("currDate")).isEqualTo(ScalarType.DATE);
        assertThat(infer("2026-05-23")).isEqualTo(ScalarType.DATE);
        assertThat(infer("currTime")).isEqualTo(ScalarType.TIME);
        assertThat(infer("10:15:30")).isEqualTo(ScalarType.TIME);
        assertThat(infer("currDateTime")).isEqualTo(ScalarType.DATETIME);
        assertThat(infer("2026-05-23T10:15:30")).isEqualTo(ScalarType.DATETIME);
        assertThat(infer("2026-05-23T10:15:30Z")).isEqualTo(ScalarType.DATETIME);
    }

    @Test
    void shouldReturnUnknownForUnrecognizedLiteral() {
        assertThat(infer("not-a-literal")).isEqualTo(UnknownType.INSTANCE);
    }

    private static Object infer(String value) {
        return LiteralTypeInferencer.infer(new LiteralNode(new NodeId("literal-" + value), SPAN, value));
    }
}
