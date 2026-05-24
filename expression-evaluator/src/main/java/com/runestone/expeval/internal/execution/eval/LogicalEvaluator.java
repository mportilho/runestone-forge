package com.runestone.expeval.internal.execution.eval;

import com.runestone.expeval.internal.compiler.CompiledExpression;
import com.runestone.expeval.internal.runtime.RuntimeServices;

import java.math.MathContext;

public final class LogicalEvaluator extends AbstractObjectEvaluator<Boolean> {

    public LogicalEvaluator(CompiledExpression compiledExpression, RuntimeServices runtimeServices, MathContext mathContext) {
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
