package com.runestone.expeval.internal.runtime;

import java.util.Map;

/**
 * Common contract shared by {@link AbstractObjectEvaluator}, allowing
 * {@link ExpressionRuntimeSupport} to swap implementations without
 * touching its public API.
 */
interface Evaluator<T> {

    T evaluate(ExecutionScope scope);

    Map<String, Object> evaluateAssignments(ExecutionScope scope);
}
