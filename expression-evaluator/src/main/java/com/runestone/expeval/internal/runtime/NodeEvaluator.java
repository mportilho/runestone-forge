package com.runestone.expeval.internal.runtime;

@FunctionalInterface
interface NodeEvaluator {
    Object evaluate(ExecutableNode node, ExecutionScope scope);
}
