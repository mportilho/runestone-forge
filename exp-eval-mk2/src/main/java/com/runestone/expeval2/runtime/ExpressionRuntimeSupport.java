package com.runestone.expeval2.runtime;

import com.runestone.expeval2.compiler.CompiledExpression;
import com.runestone.expeval2.environment.ExpressionEnvironment;

import java.math.BigDecimal;
import java.util.Objects;

public final class ExpressionRuntimeSupport {

    private final CompiledExpression compiledExpression;
    private final MutableBindings bindings;
    private final RuntimeValueFactory runtimeValueFactory;
    private final RuntimeCoercionService runtimeCoercionService;

    private ExpressionRuntimeSupport(CompiledExpression compiledExpression, MutableBindings bindings,
                                     RuntimeValueFactory runtimeValueFactory, RuntimeCoercionService runtimeCoercionService) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.bindings = Objects.requireNonNull(bindings, "bindings must not be null");
        this.runtimeValueFactory = Objects.requireNonNull(runtimeValueFactory, "runtimeValueFactory must not be null");
        this.runtimeCoercionService = Objects.requireNonNull(runtimeCoercionService, "runtimeCoercionService must not be null");
    }

    public static ExpressionRuntimeSupport from(CompiledExpression compiledExpression, ExpressionEnvironment environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        return new ExpressionRuntimeSupport(
                compiledExpression,
                MutableBindings.from(
                        compiledExpression.semanticModel(),
                        environment.externalSymbolCatalog(),
                        environment.runtimeValueFactory()
                ),
                environment.runtimeValueFactory(),
                environment.runtimeCoercionService()
        );
    }

    public void setValue(String symbolName, Object rawValue) {
        bindings.setValue(symbolName, rawValue);
    }

    public ExecutionScope createExecutionScope() {
        return ExecutionScope.from(bindings.snapshot());
    }

    public CompiledExpression compiledExpression() {
        return compiledExpression;
    }

    public BigDecimal computeMath() {
        MathEvaluator mathEvaluator = new MathEvaluator(compiledExpression, runtimeValueFactory, runtimeCoercionService);
        return mathEvaluator.evaluate(createExecutionScope());
    }

    public boolean computeLogical() {
        LogicalEvaluator logicalEvaluator = new LogicalEvaluator(compiledExpression, runtimeValueFactory, runtimeCoercionService);
        return logicalEvaluator.evaluate(createExecutionScope());
    }
}
