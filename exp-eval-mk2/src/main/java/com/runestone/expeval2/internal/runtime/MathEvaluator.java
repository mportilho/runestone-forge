package com.runestone.expeval2.internal.runtime;

import java.math.BigDecimal;
import java.math.MathContext;

final class MathEvaluator extends AbstractRuntimeEvaluator<BigDecimal> {

    MathEvaluator(CompiledExpression compiledExpression, RuntimeValueFactory runtimeValueFactory,
                  RuntimeCoercionService runtimeCoercionService, MathContext mathContext) {
        super(compiledExpression, runtimeValueFactory, runtimeCoercionService, mathContext);
    }

    @Override
    protected BigDecimal convertResult(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.NumberValue numberValue -> numberValue.value();
            default -> throw new IllegalStateException("expression did not produce a numeric result");
        };
    }
}
