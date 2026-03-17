package com.runestone.expeval2.internal.ast;

import java.util.List;
import java.util.Objects;

public final class AstValidation {

    private AstValidation() {
    }

    static void requireNodeId(NodeId nodeId) {
        Objects.requireNonNull(nodeId, "nodeId must not be null");
    }

    static void requireSourceSpan(SourceSpan sourceSpan) {
        Objects.requireNonNull(sourceSpan, "sourceSpan must not be null");
    }

    static <T> T requireNonNull(T value, String fieldName) {
        return Objects.requireNonNull(value, fieldName + " must not be null");
    }

    static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    static <T> List<T> copyList(List<T> values, String fieldName) {
        Objects.requireNonNull(values, fieldName + " must not be null");
        return List.copyOf(values);
    }
}
