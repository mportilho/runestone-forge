package com.runestone.expeval_mk3.internal.ast;

import java.util.Objects;

public final class NodeId {

    static final NodeId UNASSIGNED = new NodeId(-1, true);

    private final int value;

    public NodeId(int value) {
        this(value, false);
    }

    private NodeId(int value, boolean unassignedSentinel) {
        if (unassignedSentinel) {
            this.value = value;
            return;
        }
        if (value < 0) {
            throw new IllegalArgumentException("value must not be negative");
        }
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof NodeId that)) {
            return false;
        }
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "NodeId[value=" + value + ']';
    }
}
