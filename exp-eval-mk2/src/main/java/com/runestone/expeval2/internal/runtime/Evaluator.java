package com.runestone.expeval2.internal.runtime;

import java.util.Map;

/**
 * Common contract shared by {@link AbstractRuntimeEvaluator} and
 * {@link AbstractRawObjectEvaluator}, allowing {@link ExpressionRuntimeSupport}
 * to swap between the two implementations without touching its public API.
 */
interface Evaluator<T> {

    T evaluate(ExecutionScope scope);

    Map<String, Object> evaluateAssignments(ExecutionScope scope);
}
