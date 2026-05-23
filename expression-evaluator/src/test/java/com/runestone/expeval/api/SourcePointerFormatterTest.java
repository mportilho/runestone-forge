package com.runestone.expeval.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourcePointerFormatterTest {

    @Test
    @DisplayName("formats source line, caret span, issue code, position, and message")
    void formatsSourcePointer() {
        String formatted = SourcePointerFormatter.format(
                "a + b\nmissing()",
                new CompilationPosition(2, 0, 7),
                IssueCode.UNKNOWN_FUNCTION,
                "function not found");

        assertThat(formatted).isEqualTo(
                "  missing()\n  ^^^^^^^\n  UNKNOWN_FUNCTION at 2:0 \u2014 function not found");
    }

    @Test
    @DisplayName("uses at least one caret when position has zero width")
    void usesAtLeastOneCaret() {
        String formatted = SourcePointerFormatter.format(
                "1 +",
                new CompilationPosition(1, 3, 3),
                IssueCode.SYNTAX_ERROR,
                "unexpected end");

        assertThat(formatted).contains("   ^");
    }
}
