package com.runestone.expeval2.runtime;

import com.runestone.expeval2.compiler.CompiledExpression;
import com.runestone.expeval2.runtime.values.NumberValue;
import com.runestone.expeval2.runtime.values.RuntimeValue;

import java.math.BigDecimal;

final class MathEvaluator extends AbstractRuntimeEvaluator<BigDecimal> {

    MathEvaluator(CompiledExpression compiledExpression, RuntimeValueFactory runtimeValueFactory, RuntimeCoercionService runtimeCoercionService) {
        super(compiledExpression, runtimeValueFactory, runtimeCoercionService);
    }

    @Override
    protected BigDecimal convertResult(RuntimeValue value) {
        return switch (value) {
            case NumberValue numberValue -> numberValue.value();
            default -> throw new IllegalStateException("expression did not produce a numeric result");
        };
    }
}
