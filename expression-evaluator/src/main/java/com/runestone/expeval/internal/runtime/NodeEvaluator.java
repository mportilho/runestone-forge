package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.plan.ExecutableNode;

@FunctionalInterface
interface NodeEvaluator {
    Object evaluate(ExecutableNode node, ExecutionScope scope);
}
