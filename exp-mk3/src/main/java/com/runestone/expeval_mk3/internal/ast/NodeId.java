package com.runestone.expeval_mk3.internal.ast;

record NodeId(int value) {

    static final NodeId UNASSIGNED = new NodeId(-1);

    NodeId {
        if (value < -1) {
            throw new IllegalArgumentException("value must be -1 or greater");
        }
    }
}
