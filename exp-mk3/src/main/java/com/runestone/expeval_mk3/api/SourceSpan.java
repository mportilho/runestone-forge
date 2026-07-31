package com.runestone.expeval_mk3.api;

public record SourceSpan(int offset, int endOffset, int line, int column) {

    public SourceSpan {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must not be negative");
        }
        if (endOffset < offset) {
            throw new IllegalArgumentException("endOffset must be greater than or equal to offset");
        }
        if (line < 1) {
            throw new IllegalArgumentException("line must be one-based");
        }
        if (column < 1) {
            throw new IllegalArgumentException("column must be one-based");
        }
    }
}
