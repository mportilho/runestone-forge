package com.runestone.expeval.internal.execution.eval;

import java.util.Map;

/**
 * Common contract shared by {@link AbstractObjectEvaluator}, allowing
 * {@link com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport} to swap implementations without
 * touching its public API.
 */
public interface Evaluator<T> {

    T evaluate(ExecutionScope scope);

    Map<String, Object> evaluateAssignments(ExecutionScope scope);
}
