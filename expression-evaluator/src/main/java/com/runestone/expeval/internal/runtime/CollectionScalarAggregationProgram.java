package com.runestone.expeval.internal.runtime;

interface CollectionScalarAggregationProgram {

    int startIndex();

    Object compute(Object current, ExecutionScope scope, ScalarAggregationRuntime runtime);
}
