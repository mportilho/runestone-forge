package com.runestone.expeval2.runtime;

import com.runestone.expeval2.compiler.CompiledExpression;

final class LogicalEvaluator extends AbstractRuntimeEvaluator<Boolean> {

    LogicalEvaluator(CompiledExpression compiledExpression, RuntimeValueFactory runtimeValueFactory, RuntimeCoercionService runtimeCoercionService) {
        super(compiledExpression, runtimeValueFactory, runtimeCoercionService);
    }

    @Override
    protected Boolean convertResult(RuntimeValue value) {
        return switch (value) {
            case RuntimeValue.BooleanValue booleanValue -> booleanValue.value();
            default -> throw new IllegalStateException("expression did not produce a logical result");
        };
    }
}
