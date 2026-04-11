package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval.internal.runtime.ExpressionCompiler;
import com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport;

import java.util.Map;
import java.util.Objects;

/**
 * A compiled, reusable assignment block expression.
 *
 * <p>An assignment block is a sequence of variable assignments whose computed values are returned
 * as a {@code Map<String, Object>} keyed by symbol name. Instances are obtained via the static
 * {@code compile} factory methods. Once compiled, the same instance can be evaluated repeatedly
 * by calling {@link #compute(Map)} with the desired variable values.
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
public final class AssignmentExpression {

    private final ExpressionRuntimeSupport runtime;

    private AssignmentExpression(ExpressionRuntimeSupport runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime must not be null");
    }

    /**
     * Compiles an assignment block with an empty environment using the JVM-wide singleton compiler.
     *
     * @param source expression source text; must not be blank
     * @return a compiled expression ready for evaluation
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static AssignmentExpression compile(String source) {
        return compile(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Compiles an assignment block with the given environment using the JVM-wide singleton compiler.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment providing functions and external symbols;
     *                    must not be {@code null}
     * @return a compiled expression ready for evaluation
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static AssignmentExpression compile(String source, ExpressionEnvironment environment) {
        return new AssignmentExpression(ExpressionRuntimeSupport.compileAssignments(source, environment));
    }

    /**
     * Compiles an assignment block with the given environment using an explicit compiler.
     *
     * <p>Use this overload when the compiler is managed externally (DI / Spring {@code @Bean})
     * and its cache lifecycle must be controlled independently of the JVM-wide singleton:
     *
     * <pre>{@code
     * @Autowired ExpressionCompiler compiler;
     *
     * AssignmentExpression expr = AssignmentExpression.compile("x = a + b", environment, compiler);
     * }</pre>
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment; must not be {@code null}
     * @param compiler    compiler instance to use; must not be {@code null}
     * @return a compiled expression ready for evaluation
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static AssignmentExpression compile(String source, ExpressionEnvironment environment, ExpressionCompiler compiler) {
        return new AssignmentExpression(ExpressionRuntimeSupport.compileAssignments(source, environment, compiler));
    }

    /**
     * Validates an assignment block source text without producing an evaluable result.
     *
     * @param source expression source text; must not be blank
     * @return a {@link ValidationResult} describing any compilation issues found
     */
    public static ValidationResult validate(String source) {
        return validate(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Validates an assignment block source text against the given environment.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment to validate against; must not be {@code null}
     * @return a {@link ValidationResult} describing any compilation issues found
     */
    public static ValidationResult validate(String source, ExpressionEnvironment environment) {
        return ExpressionRuntimeSupport.validateAssignments(source, environment);
    }

    /**
     * Evaluates the assignment block with the given variable values and returns the computed
     * symbol values.
     *
     * <p>The map keys are symbol names as they appear in the expression; values are coerced to
     * the types declared in the environment. Each call receives an isolated execution scope, so
     * this method is safe to call concurrently on the same instance.
     *
     * <pre>{@code
     * Map<String, Object> result = expr.compute(Map.of("base", 100, "rate", 0.05));
     * }</pre>
     *
     * @param values variable bindings for this evaluation; must not be {@code null}
     * @return a {@code Map} from symbol name to computed value for each assignment in the block
     */
    public Map<String, Object> compute(Map<String, Object> values) {
        return runtime.computeAssignments(values);
    }

    /**
     * Evaluates the assignment block with no variable bindings and returns the computed symbol
     * values.
     *
     * <p>Convenience overload for assignment blocks that contain only literals or catalog symbols
     * whose defaults are fully specified in the environment.
     *
     * @return a {@code Map} from symbol name to computed value for each assignment in the block
     */
    public Map<String, Object> compute() {
        return compute(Map.of());
    }

    /**
     * Evaluates the assignment block with the given variable values and returns the computed
     * symbol values together with a full audit trace.
     *
     * @param values variable bindings for this evaluation; must not be {@code null}
     * @return the result map and an {@link ExpressionAuditTrace} describing each evaluation step
     */
    public AuditResult<Map<String, Object>> computeWithAudit(Map<String, Object> values) {
        return runtime.computeAssignmentsWithAudit(values);
    }

    /**
     * Evaluates the assignment block with no variable bindings and returns the computed symbol
     * values together with a full audit trace.
     *
     * @return the result map and an {@link ExpressionAuditTrace} describing each evaluation step
     */
    public AuditResult<Map<String, Object>> computeWithAudit() {
        return computeWithAudit(Map.of());
    }
}
