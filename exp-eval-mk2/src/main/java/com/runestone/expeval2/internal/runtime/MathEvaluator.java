package com.runestone.expeval2.internal.runtime;

import java.math.BigDecimal;
import java.math.MathContext;

final class MathEvaluator extends AbstractRawObjectEvaluator<BigDecimal> {

    MathEvaluator(CompiledExpression compiledExpression, RuntimeServices runtimeServices, MathContext mathContext) {
        super(compiledExpression, runtimeServices, mathContext);
    }

    @Override
    protected BigDecimal convertResult(Object rawValue) {
        if (rawValue instanceof BigDecimal bd) {
            return bd;
        }
        throw new IllegalStateException("expression did not produce a numeric result");
    }
}
