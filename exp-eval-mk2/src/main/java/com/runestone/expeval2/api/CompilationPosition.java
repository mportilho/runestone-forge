package com.runestone.expeval2.api;

public record CompilationPosition(int line, int column, int endColumn) {

    public CompilationPosition {
        if (line < 1) {
            throw new IllegalArgumentException("line must be greater than zero");
        }
        if (column < 0 || endColumn < 0) {
            throw new IllegalArgumentException("column numbers must be zero or greater");
        }
    }
}
