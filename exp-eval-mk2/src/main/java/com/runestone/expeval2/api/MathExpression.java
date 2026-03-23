package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.runtime.ExpressionCompiler;
import com.runestone.expeval2.internal.runtime.ExpressionRuntimeSupport;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/**
 * A compiled, reusable mathematical expression.
 *
 * <p>Instances are obtained via the static {@code compile} factory methods. Once compiled, the same
 * instance can be evaluated repeatedly by calling {@link #compute(Map)} with the desired variable
 * values.
 *
 * <h2>Singleton vs. injected compiler</h2>
 * <p>The two-argument overload {@link #compile(String, ExpressionEnvironment)} uses the JVM-wide
 * singleton {@link ExpressionCompiler}. Use the three-argument overload
 * {@link #compile(String, ExpressionEnvironment, ExpressionCompiler)} when the compiler must be
 * managed externally — for example in tests that need isolated caches or in a Spring application
 * where the compiler is declared as a {@code @Bean}.
 *
 * <h2>Thread safety</h2>
 * <p>Instances are thread-safe. The same compiled instance may be shared and evaluated
 * concurrently; each {@link #compute(Map)} call receives its own isolated execution scope.
 */
public final class MathExpression {

    private final ExpressionRuntimeSupport runtime;

    private MathExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
    }

    /**
     * Compiles a math expression with an empty environment using the JVM-wide singleton compiler.
     *
     * @param source expression source text; must not be blank
     * @return a compiled expression ready for evaluation
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static MathExpression compile(String source) {
        return compile(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Compiles a math expression with the given environment using the JVM-wide singleton compiler.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment providing functions and external symbols;
     *                    must not be {@code null}
     * @return a compiled expression ready for evaluation
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static MathExpression compile(String source, ExpressionEnvironment environment) {
        return new MathExpression(ExpressionRuntimeSupport.compileMath(source, environment));
    }

    /**
     * Compiles a math expression with the given environment using an explicit compiler.
     *
     * <p>Use this overload when the compiler is managed externally (DI / Spring {@code @Bean})
     * and its cache lifecycle must be controlled independently of the JVM-wide singleton:
     *
     * <pre>{@code
     * @Autowired ExpressionCompiler compiler;
     *
     * MathExpression expr = MathExpression.compile("a + b", environment, compiler);
     * }</pre>
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment; must not be {@code null}
     * @param compiler    compiler instance to use; must not be {@code null}
     * @return a compiled expression ready for evaluation
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static MathExpression compile(String source, ExpressionEnvironment environment, ExpressionCompiler compiler) {
        return new MathExpression(ExpressionRuntimeSupport.compileMath(source, environment, compiler));
    }

    /**
     * Validates a math expression source text without producing an evaluable result.
     *
     * @param source expression source text; must not be blank
     * @return a {@link ValidationResult} describing any compilation issues found
     */
    public static ValidationResult validate(String source) {
        return validate(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Validates a math expression source text against the given environment.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment to validate against; must not be {@code null}
     * @return a {@link ValidationResult} describing any compilation issues found
     */
    public static ValidationResult validate(String source, ExpressionEnvironment environment) {
        return ExpressionRuntimeSupport.validateMath(source, environment);
    }

    /**
     * Evaluates the expression with the given variable values and returns the numeric result.
     *
     * <p>The map keys are symbol names as they appear in the expression; values are coerced to
     * the types declared in the environment. Each call receives an isolated execution scope, so
     * this method is safe to call concurrently on the same instance.
     *
     * <pre>{@code
     * BigDecimal result = expr.compute(Map.of("a", 10, "b", 5));
     * }</pre>
     *
     * @param values variable bindings for this evaluation; must not be {@code null}
     * @return the computed {@link BigDecimal} result
     */
    public BigDecimal compute(Map<String, Object> values) {
        return runtime.computeMath(values);
    }

    /**
     * Evaluates the expression with no variable bindings and returns the numeric result.
     *
     * <p>Convenience overload for expressions that contain only literals or catalog symbols
     * whose defaults are fully specified in the environment.
     *
     * @return the computed {@link BigDecimal} result
     */
    public BigDecimal compute() {
        return compute(Map.of());
    }

    /**
     * Evaluates the expression with the given variable values and returns the numeric result
     * together with a full audit trace.
     *
     * @param values variable bindings for this evaluation; must not be {@code null}
     * @return the result and an {@link ExpressionAuditTrace} describing each evaluation step
     */
    public AuditResult<BigDecimal> computeWithAudit(Map<String, Object> values) {
        return runtime.computeMathWithAudit(values);
    }

    /**
     * Evaluates the expression with no variable bindings and returns the numeric result together
     * with a full audit trace.
     *
     * @return the result and an {@link ExpressionAuditTrace} describing each evaluation step
     */
    public AuditResult<BigDecimal> computeWithAudit() {
        return computeWithAudit(Map.of());
    }
}
