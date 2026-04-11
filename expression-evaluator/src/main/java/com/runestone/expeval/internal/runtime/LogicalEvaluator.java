package com.runestone.expeval.internal.runtime;

import java.math.MathContext;

final class LogicalEvaluator extends AbstractObjectEvaluator<Boolean> {

    LogicalEvaluator(CompiledExpression compiledExpression, RuntimeServices runtimeServices, MathContext mathContext) {
        super(compiledExpression, runtimeServices, mathContext);
    }

    @Override
    protected Boolean convertResult(Object value) {
        if (value instanceof Boolean b) {
            return b;
        }
        throw new IllegalStateException("expression did not produce a logical result");
    }
}
