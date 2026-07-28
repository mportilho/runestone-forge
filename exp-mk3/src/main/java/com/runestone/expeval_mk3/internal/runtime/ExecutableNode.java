package com.runestone.expeval_mk3.internal.runtime;

@FunctionalInterface
public interface ExecutableNode {

    Object execute(ExecutionScope scope);
}
