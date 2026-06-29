package com.runestone.expeval.internal.runtime;

import java.math.MathContext;
import java.util.Objects;

record ScalarAggregationRuntime(
        String source,
        String rootName,
        RuntimeServices runtimeServices,
        MathContext mathContext,
        NodeEvaluator evaluator
) {

    ScalarAggregationRuntime {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(rootName, "rootName must not be null");
        Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");
        Objects.requireNonNull(mathContext, "mathContext must not be null");
        Objects.requireNonNull(evaluator, "evaluator must not be null");
    }
}
