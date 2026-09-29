package com.runestone.expeval.internal.runtime;

import java.util.Objects;

public record ExecutableLambda(ExecutableNode body, int currentItemSlot) {

    public ExecutableLambda {
        Objects.requireNonNull(body, "body");
    }

    Object execute(ExecutionScope scope, Object currentItem) {
        Object previous = scope.replace(currentItemSlot, currentItem);
        try {
            return body.execute(scope);
        } finally {
            scope.restore(currentItemSlot, previous);
        }
    }
}
