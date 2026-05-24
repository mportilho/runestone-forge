package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.internal.execution.eval.*;
import com.runestone.expeval.internal.execution.plan.*;
import com.runestone.expeval.internal.compiler.CompiledExpression;
import com.runestone.expeval.internal.navigation.PropertyChainOps;

import java.math.MathContext;
import java.time.LocalDate;
import java.util.*;

/**
 * Expression evaluator that carries all sub-expression results as {@code Object} values
 * ({@link java.math.BigDecimal}, {@link Boolean}, {@link LocalDate}, etc.) with no intermediate boxing.
 *
 * <h2>Scope contract</h2>
 * <ul>
 *   <li><strong>Scope read</strong> — variables are looked up via
 *       {@link ExecutionScope#find}, which avoids both {@link java.util.Optional} and wrapper
 *       allocation.</li>
 *   <li><strong>Scope write (assignments)</strong> — the result is stored directly via
 *       {@link ExecutionScope#assign}.  The scope handles null-value sentinels internally.</li>
 *   <li><strong>Function arguments</strong> — coerced via
 *       {@link RuntimeServices#coerce}, which fast-paths same-type arguments.</li>
 * </ul>
 */
abstract class AbstractObjectEvaluator<T> implements Evaluator<T> {

    private final CompiledExpression compiledExpression;
    private final RuntimeServices runtimeServices;
    private final MathContext mathContext;
    private final String source;
    private final NodeEvaluator nodeEvaluator;
    private final AssignmentEvaluator assignmentEvaluator;
    private final SymbolValueEvaluator symbolValueEvaluator;
    private final FunctionCallEvaluator functionCallEvaluator;
    private final StructuredExpressionEvaluator structuredExpressionEvaluator;

    protected AbstractObjectEvaluator(CompiledExpression compiledExpression,
                                      RuntimeServices runtimeServices,
                                      MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");
        this.mathContext = Objects.requireNonNull(mathContext, "mathContext must not be null");
        this.source = compiledExpression.source();
        this.nodeEvaluator = this::evaluateExpr;
        this.assignmentEvaluator = new AssignmentEvaluator(nodeEvaluator);
        this.symbolValueEvaluator = new SymbolValueEvaluator(source);
        this.functionCallEvaluator = new FunctionCallEvaluator(runtimeServices, nodeEvaluator);
        this.structuredExpressionEvaluator = new StructuredExpressionEvaluator(
                source, runtimeServices, mathContext, nodeEvaluator);
    }

    @Override
    public final T evaluate(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        ExecutionPlan plan = compiledExpression.executionPlan();
        assignmentEvaluator.execute(plan.assignments(), scope);
        return convertResult(evaluateExpr(plan.resultExpression(), scope));
    }

    /**
     * Converts the final result to the evaluator's declared return type.
     */
    protected abstract T convertResult(Object value);

    @Override
    public final Map<String, Object> evaluateAssignments(ExecutionScope scope) {
        Objects.requireNonNull(scope, "scope must not be null");
        ExecutionPlan plan = compiledExpression.executionPlan();
        return assignmentEvaluator.evaluateAssignments(plan.assignments(), scope);
    }

    // -------------------------------------------------------------------------
    // Expression evaluation — returns Object, no boxing
    // -------------------------------------------------------------------------

    private Object evaluateExpr(ExecutableNode node, ExecutionScope scope) {
        return switch (node) {
            case ExecutableLiteral lit -> lit.precomputed();
            case ExecutableDynamicLiteral dyn -> symbolValueEvaluator.evaluateDynamicLiteral(dyn, scope);
            case ExecutableIdentifier id -> symbolValueEvaluator.evaluateIdentifier(id, scope);
            case ExecutablePropertyChain chain ->
                    PropertyChainOps.evaluatePropertyChain(chain, scope, source, runtimeServices, mathContext, nodeEvaluator);
            case ExecutableFunctionCall f -> functionCallEvaluator.evaluate(f, scope);
            case ExecutableConditional c -> structuredExpressionEvaluator.evaluateConditional(c, scope);
            case ExecutableSimpleConditional sc -> structuredExpressionEvaluator.evaluateSimpleConditional(sc, scope);
            case ExecutableUnaryOp u -> structuredExpressionEvaluator.evaluateUnary(u, scope);
            case ExecutableBinaryOp b -> structuredExpressionEvaluator.evaluateBinary(b, scope);
            case ExecutableTernaryOp t -> structuredExpressionEvaluator.evaluateTernary(t, scope);
            case ExecutablePostfixOp p -> structuredExpressionEvaluator.evaluatePostfix(p, scope);
            case ExecutableVectorLiteral v -> structuredExpressionEvaluator.evaluateVector(v, scope);
            case ExecutableNullCoalesce nc -> structuredExpressionEvaluator.evaluateNullCoalesce(nc, scope);
            case ExecutableRegexOp r -> structuredExpressionEvaluator.evaluateRegex(r, scope);
        };
    }

}
