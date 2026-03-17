package com.runestone.expeval2.internal.ast;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SourceSpanTest {

    @Test
    void shouldPreserveSourceCoordinates() {
        SourceSpan sourceSpan = new SourceSpan(2, 8, 1, 3, 1, 9);

        assertThat(sourceSpan.startOffset()).isEqualTo(2);
        assertThat(sourceSpan.endOffset()).isEqualTo(8);
        assertThat(sourceSpan.startLine()).isEqualTo(1);
        assertThat(sourceSpan.startColumn()).isEqualTo(3);
        assertThat(sourceSpan.endLine()).isEqualTo(1);
        assertThat(sourceSpan.endColumn()).isEqualTo(9);
    }

    @Test
    void shouldRejectNegativeOffset() {
        assertThatThrownBy(() -> new SourceSpan(-1, 8, 1, 1, 1, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("offsets must be non-negative");
    }

    @Test
    void shouldRejectLineNumbersLowerThanOne() {
        assertThatThrownBy(() -> new SourceSpan(0, 8, 0, 1, 1, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("line numbers must be greater than zero");
    }

    @Test
    void shouldRejectColumnNumbersLowerThanZero() {
        assertThatThrownBy(() -> new SourceSpan(0, 8, 1, -1, 1, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("column numbers must be zero or greater");
    }

    @Test
    void shouldRejectEndOffsetBeforeStartOffset() {
        assertThatThrownBy(() -> new SourceSpan(9, 8, 1, 1, 1, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("endOffset must be greater than or equal to startOffset");
    }

    @Test
    void shouldRejectEndingPositionBeforeStartingPosition() {
        assertThatThrownBy(() -> new SourceSpan(1, 8, 2, 1, 1, 5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ending position must not be before starting position");
    }
}
