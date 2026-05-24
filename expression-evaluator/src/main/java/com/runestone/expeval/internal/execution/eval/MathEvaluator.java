package com.runestone.expeval.internal.execution.eval;

import com.runestone.expeval.internal.compiler.CompiledExpression;
import com.runestone.expeval.internal.runtime.RuntimeServices;

import java.math.BigDecimal;
import java.math.MathContext;

public final class MathEvaluator extends AbstractObjectEvaluator<BigDecimal> {

    public MathEvaluator(CompiledExpression compiledExpression, RuntimeServices runtimeServices, MathContext mathContext) {
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
