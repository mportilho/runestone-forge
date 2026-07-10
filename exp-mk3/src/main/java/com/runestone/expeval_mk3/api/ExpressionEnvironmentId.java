package com.runestone.expeval_mk3.api;

/**
 * Stable canonical identity for an {@link ExpressionEnvironment}.
 */
public final class ExpressionEnvironmentId {

    private final String value;

    ExpressionEnvironmentId(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        return object instanceof ExpressionEnvironmentId other && value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
