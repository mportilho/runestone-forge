package com.runestone.expeval.internal.runtime;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ExecutableVectorLiteral implements ExecutableNode {

    private final List<ExecutableNode> elements;
    private final List<Object> foldedValue;

    public ExecutableVectorLiteral(List<ExecutableNode> elements) {
        this(elements, null);
    }

    public ExecutableVectorLiteral(List<ExecutableNode> elements, List<Object> foldedValue) {
        this.elements = Collections.unmodifiableList(Objects.requireNonNull(elements, "elements must not be null"));
        this.foldedValue = foldedValue != null ? Collections.unmodifiableList(foldedValue) : null;
    }

    public List<ExecutableNode> elements() {
        return elements;
    }

    public List<Object> foldedValue() {
        return foldedValue;
    }

    public boolean isFolded() {
        return foldedValue != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutableVectorLiteral that = (ExecutableVectorLiteral) o;
        return Objects.equals(elements, that.elements) && Objects.equals(foldedValue, that.foldedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements, foldedValue);
    }

    @Override
    public String toString() {
        return "ExecutableVectorLiteral[elements=" + elements + ", foldedValue=" + foldedValue + "]";
    }
}
