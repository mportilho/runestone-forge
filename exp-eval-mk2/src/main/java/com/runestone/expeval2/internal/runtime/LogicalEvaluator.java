package com.runestone.expeval2.internal.runtime;

import java.math.MathContext;

final class LogicalEvaluator extends AbstractRawObjectEvaluator<Boolean> {

    LogicalEvaluator(CompiledExpression compiledExpression, RuntimeServices runtimeServices, MathContext mathContext) {
        super(compiledExpression, runtimeServices, mathContext);
    }

    @Override
    protected Boolean convertResult(Object rawValue) {
        if (rawValue instanceof Boolean b) {
            return b;
        }
        throw new IllegalStateException("expression did not produce a logical result");
    }
}
