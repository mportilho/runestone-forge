package com.runestone.expeval2.ast;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NodeIdTest {

    @Test
    void shouldPreserveCanonicalValue() {
        NodeId nodeId = new NodeId("node-1");

        assertThat(nodeId.value()).isEqualTo("node-1");
    }

    @Test
    void shouldRejectNullValue() {
        assertThatThrownBy(() -> new NodeId(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("value must not be null");
    }

    @Test
    void shouldRejectBlankValue() {
        assertThatThrownBy(() -> new NodeId("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("value must not be blank");
    }
}
