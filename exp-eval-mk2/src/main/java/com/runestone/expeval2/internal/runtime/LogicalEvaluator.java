package com.runestone.expeval2.internal.runtime;

import java.math.MathContext;

final class LogicalEvaluator extends AbstractRuntimeEvaluator<Boolean> {

    LogicalEvaluator(CompiledExpression compiledExpression, RuntimeValueFactory runtimeValueFactory,
                     RuntimeCoercionService runtimeCoercionService, MathContext mathContext) {
        super(compiledExpression, runtimeValueFactory, runtimeCoercionService, mathContext);
    }

    @Override
    protected Boolean convertResult(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.BooleanValue booleanValue -> booleanValue.value();
            default -> throw new IllegalStateException("expression did not produce a logical result");
        };
    }
}
