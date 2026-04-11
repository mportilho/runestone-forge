package com.runestone.expeval.internal.runtime;

import java.math.BigDecimal;
import java.math.MathContext;

final class MathEvaluator extends AbstractObjectEvaluator<BigDecimal> {

    MathEvaluator(CompiledExpression compiledExpression, RuntimeServices runtimeServices, MathContext mathContext) {
        super(compiledExpression, runtimeServices, mathContext);
    }

    @Override
    protected BigDecimal convertResult(Object value) {
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        throw new IllegalStateException("expression did not produce a numeric result");
    }
}
