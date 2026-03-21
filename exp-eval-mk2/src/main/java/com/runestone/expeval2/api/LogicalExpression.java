package com.runestone.expeval2.api;

import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval2.internal.runtime.ExpressionCompiler;
import com.runestone.expeval2.internal.runtime.ExpressionRuntimeSupport;

import java.util.Objects;

/**
 * A compiled, reusable logical expression.
 *
 * <p>Instances are obtained via the static {@code compile} factory methods. Once compiled, the same
 * instance can be evaluated repeatedly with different variable values by calling {@link #setValue}
 * followed by {@link #compute}.
 *
 * <h2>Singleton vs. injected compiler</h2>
 * <p>The two-argument overload {@link #compile(String, ExpressionEnvironment)} uses the JVM-wide
 * singleton {@link ExpressionCompiler}. Use the three-argument overload
 * {@link #compile(String, ExpressionEnvironment, ExpressionCompiler)} when the compiler must be
 * managed externally — for example in tests that need isolated caches or in a Spring application
 * where the compiler is declared as a {@code @Bean}.
 *
 * <h2>Thread safety</h2>
 * <p>Instances are <em>not</em> thread-safe. Do not share a single instance across threads;
 * instead, compile once and create one instance per thread (or per request).
 */
public final class LogicalExpression {

    private final ExpressionRuntimeSupport runtime;

    private LogicalExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
    }

    /**
     * Compiles a logical expression with an empty environment using the JVM-wide singleton compiler.
     *
     * @param source expression source text; must not be blank
     * @return a compiled expression ready for evaluation
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static LogicalExpression compile(String source) {
        return compile(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Compiles a logical expression with the given environment using the JVM-wide singleton compiler.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment providing functions and external symbols;
     *                    must not be {@code null}
     * @return a compiled expression ready for evaluation
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static LogicalExpression compile(String source, ExpressionEnvironment environment) {
        return new LogicalExpression(ExpressionRuntimeSupport.compileLogical(source, environment));
    }

    /**
     * Compiles a logical expression with the given environment using an explicit compiler.
     *
     * <p>Use this overload when the compiler is managed externally (DI / Spring {@code @Bean})
     * and its cache lifecycle must be controlled independently of the JVM-wide singleton:
     *
     * <pre>{@code
     * @Autowired ExpressionCompiler compiler;
     *
     * LogicalExpression expr = LogicalExpression.compile("a > b", environment, compiler);
     * }</pre>
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment; must not be {@code null}
     * @param compiler    compiler instance to use; must not be {@code null}
     * @return a compiled expression ready for evaluation
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static LogicalExpression compile(String source, ExpressionEnvironment environment, ExpressionCompiler compiler) {
        return new LogicalExpression(ExpressionRuntimeSupport.compileLogical(source, environment, compiler));
    }

    /**
     * Validates a logical expression source text without producing an evaluable result.
     *
     * @param source expression source text; must not be blank
     * @return a {@link ValidationResult} describing any compilation issues found
     */
    public static ValidationResult validate(String source) {
        return validate(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Validates a logical expression source text against the given environment.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment to validate against; must not be {@code null}
     * @return a {@link ValidationResult} describing any compilation issues found
     */
    public static ValidationResult validate(String source, ExpressionEnvironment environment) {
        return ExpressionRuntimeSupport.validateLogical(source, environment);
    }

    /**
     * Sets the value of a variable symbol before evaluation.
     *
     * <p>The value is coerced to the type declared for the symbol in the expression environment.
     * This method returns {@code this} to allow chaining:
     *
     * <pre>{@code
     * boolean result = expr.setValue("a", 10).setValue("b", 5).compute();
     * }</pre>
     *
     * @param symbolName name of the symbol as it appears in the expression; must not be {@code null}
     * @param rawValue   value to bind; coercion is applied as configured in the environment
     * @return this instance
     */
    public LogicalExpression setValue(String symbolName, Object rawValue) {
        runtime.setValue(symbolName, rawValue);
        return this;
    }

    /**
     * Evaluates the expression and returns the boolean result.
     *
     * @return the computed boolean result
     */
    public boolean compute() {
        return runtime.computeLogical();
    }

    /**
     * Evaluates the expression and returns the boolean result together with a full audit trace.
     *
     * @return the result and an {@link ExpressionAuditTrace} describing each evaluation step
     */
    public AuditResult<Boolean> computeWithAudit() {
        return runtime.computeLogicalWithAudit();
    }
}
