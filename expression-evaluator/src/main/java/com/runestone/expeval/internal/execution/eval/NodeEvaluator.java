package com.runestone.expeval.internal.execution.eval;

import com.runestone.expeval.internal.execution.plan.ExecutableNode;

@FunctionalInterface
public interface NodeEvaluator {
    Object evaluate(ExecutableNode node, ExecutionScope scope);
}
