package com.runestone.expeval.internal.ast;

public record SourceSpan(
    int startOffset,
    int endOffset,
    int startLine,
    int startColumn,
    int endLine,
    int endColumn
) {

    public SourceSpan {
        if (startOffset < 0 || endOffset < 0) {
            throw new IllegalArgumentException("offsets must be non-negative");
        }
        if (startLine < 1 || endLine < 1) {
            throw new IllegalArgumentException("line numbers must be greater than zero");
        }
        if (startColumn < 0 || endColumn < 0) {
            throw new IllegalArgumentException("column numbers must be zero or greater");
        }
        if (endOffset < startOffset) {
            throw new IllegalArgumentException("endOffset must be greater than or equal to startOffset");
        }
        if (endLine < startLine || (endLine == startLine && endColumn < startColumn)) {
            throw new IllegalArgumentException("ending position must not be before starting position");
        }
    }
}
