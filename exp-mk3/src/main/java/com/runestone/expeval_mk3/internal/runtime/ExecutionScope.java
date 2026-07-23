package com.runestone.expeval_mk3.internal.runtime;

public final class ExecutionScope {

    private final Object[] frame;

    public ExecutionScope(int frameSize) {
        frame = new Object[frameSize];
    }

    public Object read(int slot) {
        return frame[slot];
    }

    public void write(int slot, Object value) {
        frame[slot] = value;
    }
}
