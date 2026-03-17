package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.api.CompilationIssue;
import com.runestone.expeval2.api.ExpressionCompilationException;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.grammar.language.ExpressionResultType;
import com.runestone.expeval2.internal.compiler.CompiledExpression;
import com.runestone.expeval2.internal.compiler.ExpressionCompiler;
import com.runestone.expeval2.internal.semantic.SemanticResolutionException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class ExpressionRuntimeSupport {

    private static final ExpressionCompiler COMPILER = new ExpressionCompiler();

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

    public static ExpressionRuntimeSupport compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        try {
            CompiledExpression compiled = COMPILER.compile(source, resultType, environment);
            return from(compiled, environment);
        } catch (SemanticResolutionException e) {
            List<CompilationIssue> issues = e.issues().stream()
                .map(issue -> new CompilationIssue(issue.code(), issue.message()))
                .toList();
            throw new ExpressionCompilationException(source, issues, e);
        }
    }

    static ExpressionRuntimeSupport from(CompiledExpression compiledExpression, ExpressionEnvironment environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        RuntimeValueFactory runtimeValueFactory = new RuntimeValueFactory(environment.getDataConversionService());
        RuntimeCoercionService runtimeCoercionService = new RuntimeCoercionService(environment.getDataConversionService());
        return new ExpressionRuntimeSupport(
                compiledExpression,
                MutableBindings.from(
                        compiledExpression.semanticModel(),
                        environment.externalSymbolCatalog(),
                        runtimeValueFactory
                ),
                runtimeValueFactory,
                runtimeCoercionService
        );
    }

    public void setValue(String symbolName, Object rawValue) {
        bindings.setValue(symbolName, rawValue);
    }

    ExecutionScope createExecutionScope() {
        return ExecutionScope.from(bindings.snapshot());
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
