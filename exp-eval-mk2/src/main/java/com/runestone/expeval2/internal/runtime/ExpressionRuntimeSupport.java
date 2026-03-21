package com.runestone.expeval2.internal.runtime;

import com.runestone.expeval2.api.AuditResult;
import com.runestone.expeval2.api.CacheConfig;
import com.runestone.expeval2.api.CompilationIssue;
import com.runestone.expeval2.api.CompilationPosition;
import com.runestone.expeval2.api.ExpressionCompilationException;
import com.runestone.expeval2.api.ValidationResult;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.ast.SourceSpan;
import com.runestone.expeval2.internal.ast.mapping.SemanticAstBuilder;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2ParserFacade;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.internal.grammar.ParsingException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Bridge between the compilation pipeline and the public expression API.
 *
 * <p>This class serves two roles:
 * <ol>
 *   <li><strong>Static factory</strong> — the {@code compileMath}, {@code compileLogical}, and
 *       {@code compileAssignments} methods compile a source string and return a ready-to-evaluate
 *       instance backed by the JVM-wide {@link ExpressionCompiler} singleton.</li>
 *   <li><strong>Evaluation context</strong> — each instance holds the compiled plan, the mutable
 *       symbol bindings, and the evaluators for a single expression. Call {@link #setValue} to
 *       supply variable values, then {@code computeMath} / {@code computeLogical} /
 *       {@code computeAssignments} to evaluate.</li>
 * </ol>
 *
 * <h2>JVM-wide singleton lifecycle</h2>
 * <p>The singleton is lazily initialized on the first compilation. To control its configuration
 * call {@link #configure(CacheConfig)} <em>before</em> any compilation, or use
 * {@link #reconfigure(CacheConfig)} at any point to replace the singleton with a new one.
 * Use {@link #invalidateCache()} to clear cached entries without replacing the compiler.
 *
 * <h2>Compiler injection (DI)</h2>
 * <p>When the singleton model is unsuitable — for example when running multiple isolated
 * expression engines in the same JVM or in a Spring context that manages the compiler as a
 * {@code @Bean} — pass an explicit {@link ExpressionCompiler} to the overloaded compile methods:
 *
 * <pre>{@code
 * ExpressionCompiler myCompiler = new ExpressionCompiler(new CacheConfig(4_096, null));
 * ExpressionRuntimeSupport runtime =
 *         ExpressionRuntimeSupport.compileMath(source, environment, myCompiler);
 * }</pre>
 *
 * <h2>Thread safety</h2>
 * <p>The static factory methods are thread-safe. Individual instances are <em>not</em> thread-safe:
 * {@link #setValue} mutates shared bindings and must not be called concurrently on the same instance.
 */
public final class ExpressionRuntimeSupport {

    private static volatile ExpressionCompiler COMPILER;

    /**
     * Configures the JVM-wide expression compiler <strong>before the first compilation</strong>.
     *
     * <p>If the singleton has already been initialized — either by a previous call to this method
     * or lazily on the first compilation — the call is silently ignored. Use
     * {@link #reconfigure(CacheConfig)} when you need to replace an already-initialized compiler.
     *
     * <p>Typical usage (application startup or {@code main}):
     * <pre>{@code
     * ExpressionRuntimeSupport.configure(new CacheConfig(4_096, Duration.ofHours(1)));
     * }</pre>
     *
     * @param cacheConfig cache settings to apply; must not be {@code null}
     * @see #reconfigure(CacheConfig)
     */
    public static void configure(CacheConfig cacheConfig) {
        Objects.requireNonNull(cacheConfig, "cacheConfig must not be null");
        if (COMPILER == null) {
            synchronized (ExpressionRuntimeSupport.class) {
                if (COMPILER == null) {
                    COMPILER = new ExpressionCompiler(cacheConfig);
                }
            }
        }
    }

    /**
     * Replaces the JVM-wide expression compiler with a new instance built from {@code cacheConfig}.
     *
     * <p>Unlike {@link #configure(CacheConfig)}, this method always takes effect — even after the
     * singleton has been initialized. Compilations already in progress hold a reference to the old
     * compiler and complete normally; all subsequent compilations use the new instance and its
     * empty cache.
     *
     * <p>Use this method when a function-catalog update requires both a cache flush and new cache
     * settings, or when the previous {@link #configure(CacheConfig)} call must be overridden at
     * runtime:
     *
     * <pre>{@code
     * // After reloading the function catalog, start fresh with a larger cache:
     * ExpressionRuntimeSupport.reconfigure(new CacheConfig(8_192, Duration.ofHours(2)));
     * }</pre>
     *
     * @param cacheConfig new cache settings; must not be {@code null}
     * @see #configure(CacheConfig)
     * @see #invalidateCache()
     */
    public static void reconfigure(CacheConfig cacheConfig) {
        Objects.requireNonNull(cacheConfig, "cacheConfig must not be null");
        synchronized (ExpressionRuntimeSupport.class) {
            COMPILER = new ExpressionCompiler(cacheConfig);
        }
    }

    /**
     * Removes all entries from the JVM-wide compiler cache without replacing the compiler instance.
     *
     * <p>Use this when cached expressions may have become stale — for example after updating a
     * function provider or external symbol — but the current cache configuration should be kept.
     * Compilations already in progress complete normally; only subsequent cache lookups are affected.
     *
     * <p>If a full reconfiguration is also required, use {@link #reconfigure(CacheConfig)} instead,
     * which both replaces the compiler and discards its cache in a single step.
     *
     * @see #reconfigure(CacheConfig)
     */
    public static void invalidateCache() {
        getCompiler().invalidateCache();
    }

    private static ExpressionCompiler getCompiler() {
        ExpressionCompiler c = COMPILER;
        if (c == null) {
            synchronized (ExpressionRuntimeSupport.class) {
                c = COMPILER;
                if (c == null) {
                    COMPILER = c = new ExpressionCompiler();
                }
            }
        }
        return c;
    }

    private final CompiledExpression compiledExpression;
    private final MutableBindings bindings;
    private final MathEvaluator mathEvaluator;
    private final LogicalEvaluator logicalEvaluator;
    private final boolean hasAssignments;
    private final int internalSymbolCount;
    private final int maxAuditEvents;

    private ExpressionRuntimeSupport(CompiledExpression compiledExpression, MutableBindings bindings,
                                     RuntimeValueFactory runtimeValueFactory, RuntimeCoercionService runtimeCoercionService,
                                     MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.bindings = Objects.requireNonNull(bindings, "bindings must not be null");
        Objects.requireNonNull(runtimeValueFactory, "runtimeValueFactory must not be null");
        Objects.requireNonNull(runtimeCoercionService, "runtimeCoercionService must not be null");
        Objects.requireNonNull(mathContext, "mathContext must not be null");
        this.mathEvaluator = new MathEvaluator(compiledExpression, runtimeValueFactory, runtimeCoercionService, mathContext);
        this.logicalEvaluator = new LogicalEvaluator(compiledExpression, runtimeValueFactory, runtimeCoercionService, mathContext);
        this.hasAssignments = !compiledExpression.executionPlan().assignments().isEmpty();
        this.internalSymbolCount = compiledExpression.semanticModel().internalSymbolsByName().size();
        this.maxAuditEvents = countMaxAuditEvents(compiledExpression.executionPlan());
    }

    /**
     * Compiles a math expression using the JVM-wide singleton compiler.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment; must not be {@code null}
     * @return a ready-to-evaluate runtime instance
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static ExpressionRuntimeSupport compileMath(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.MATH, environment);
    }

    /**
     * Compiles a math expression using an explicit {@link ExpressionCompiler}.
     *
     * <p>Use this overload when the singleton model is unsuitable and the compiler is managed
     * externally — for example as a Spring {@code @Bean} or in isolated test contexts:
     *
     * <pre>{@code
     * ExpressionRuntimeSupport runtime =
     *         ExpressionRuntimeSupport.compileMath(source, environment, myCompiler);
     * }</pre>
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment; must not be {@code null}
     * @param compiler    compiler instance to use; must not be {@code null}
     * @return a ready-to-evaluate runtime instance
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static ExpressionRuntimeSupport compileMath(String source, ExpressionEnvironment environment, ExpressionCompiler compiler) {
        return compile(source, ExpressionResultType.MATH, environment, compiler);
    }

    /**
     * Compiles a logical expression using the JVM-wide singleton compiler.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment; must not be {@code null}
     * @return a ready-to-evaluate runtime instance
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static ExpressionRuntimeSupport compileLogical(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.LOGICAL, environment);
    }

    /**
     * Compiles a logical expression using an explicit {@link ExpressionCompiler}.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment; must not be {@code null}
     * @param compiler    compiler instance to use; must not be {@code null}
     * @return a ready-to-evaluate runtime instance
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static ExpressionRuntimeSupport compileLogical(String source, ExpressionEnvironment environment, ExpressionCompiler compiler) {
        return compile(source, ExpressionResultType.LOGICAL, environment, compiler);
    }

    /**
     * Compiles an assignment block using the JVM-wide singleton compiler.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment; must not be {@code null}
     * @return a ready-to-evaluate runtime instance
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static ExpressionRuntimeSupport compileAssignments(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.ASSIGNMENTS, environment);
    }

    /**
     * Compiles an assignment block using an explicit {@link ExpressionCompiler}.
     *
     * @param source      expression source text; must not be blank
     * @param environment execution environment; must not be {@code null}
     * @param compiler    compiler instance to use; must not be {@code null}
     * @return a ready-to-evaluate runtime instance
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static ExpressionRuntimeSupport compileAssignments(String source, ExpressionEnvironment environment, ExpressionCompiler compiler) {
        return compile(source, ExpressionResultType.ASSIGNMENTS, environment, compiler);
    }

    public static ValidationResult validateMath(String source, ExpressionEnvironment environment) {
        return validate(source, ExpressionResultType.MATH, environment);
    }

    public static ValidationResult validateLogical(String source, ExpressionEnvironment environment) {
        return validate(source, ExpressionResultType.LOGICAL, environment);
    }

    public static ValidationResult validateAssignments(String source, ExpressionEnvironment environment) {
        return validate(source, ExpressionResultType.ASSIGNMENTS, environment);
    }

    public static ValidationResult validate(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        try {
            compile(source, resultType, environment);
            return ValidationResult.ok(source);
        } catch (ExpressionCompilationException e) {
            return ValidationResult.failed(source, e.issues());
        } catch (ParsingException e) {
            List<CompilationIssue> issues = e.errors().stream()
                .map(err -> new CompilationIssue(
                    "SYNTAX_ERROR",
                    err.message(),
                    new CompilationPosition(err.line(), err.charPositionInLine(), err.charPositionInLine() + 1)
                ))
                .toList();
            return ValidationResult.failed(
                source,
                issues.isEmpty() ? List.of(new CompilationIssue("SYNTAX_ERROR", "syntax error")) : issues
            );
        }
    }

    /**
     * Compiles an expression using the JVM-wide singleton compiler.
     *
     * @param source      expression source text; must not be blank
     * @param resultType  expected result kind; must not be {@code null}
     * @param environment execution environment; must not be {@code null}
     * @return a ready-to-evaluate runtime instance
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static ExpressionRuntimeSupport compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        return compile(source, resultType, environment, getCompiler());
    }

    /**
     * Compiles an expression using an explicit {@link ExpressionCompiler}.
     *
     * <p>This is the base overload used by all other {@code compile*} methods. It is the correct
     * entry point when the compiler is managed externally (DI) and full control over caching and
     * lifecycle is required.
     *
     * @param source      expression source text; must not be blank
     * @param resultType  expected result kind; must not be {@code null}
     * @param environment execution environment; must not be {@code null}
     * @param compiler    compiler instance to use; must not be {@code null}
     * @return a ready-to-evaluate runtime instance
     * @throws ExpressionCompilationException if the expression has semantic errors
     */
    public static ExpressionRuntimeSupport compile(String source, ExpressionResultType resultType,
                                                    ExpressionEnvironment environment, ExpressionCompiler compiler) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(compiler, "compiler must not be null");
        try {
            CompiledExpression compiled = compiler.compile(source, resultType, environment);
            return from(compiled, environment);
        } catch (SemanticResolutionException e) {
            List<CompilationIssue> issues = e.issues().stream()
                .map(issue -> {
                    SourceSpan span = issue.sourceSpan();
                    CompilationPosition position = new CompilationPosition(span.startLine(), span.startColumn(), span.endColumn());
                    return new CompilationIssue(issue.code(), issue.message(), position);
                })
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
                runtimeCoercionService,
                environment.mathContext()
        );
    }

    public void setValue(String symbolName, Object rawValue) {
        bindings.setValue(symbolName, rawValue);
    }

    ExecutionScope createExecutionScope() {
        if (hasAssignments) {
            return ExecutionScope.from(bindings.valuesReadOnly(), internalSymbolCount);
        }
        return ExecutionScope.readOnly(bindings.valuesReadOnly());
    }

    private ExecutionScope createAuditedExecutionScope(AuditCollector collector) {
        if (hasAssignments) {
            return ExecutionScope.fromWithAudit(bindings.valuesReadOnly(), internalSymbolCount, collector);
        }
        return ExecutionScope.readOnlyWithAudit(bindings.valuesReadOnly(), collector);
    }

    public BigDecimal computeMath() {
        return mathEvaluator.evaluate(createExecutionScope());
    }

    public boolean computeLogical() {
        return logicalEvaluator.evaluate(createExecutionScope());
    }

    public AuditResult<BigDecimal> computeMathWithAudit() {
        AuditCollector collector = new AuditCollector(maxAuditEvents);
        BigDecimal result = mathEvaluator.evaluate(createAuditedExecutionScope(collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public AuditResult<Boolean> computeLogicalWithAudit() {
        AuditCollector collector = new AuditCollector(maxAuditEvents);
        boolean result = logicalEvaluator.evaluate(createAuditedExecutionScope(collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public Map<String, Object> computeAssignments() {
        return mathEvaluator.evaluateAssignments(createExecutionScope());
    }

    public AuditResult<Map<String, Object>> computeAssignmentsWithAudit() {
        AuditCollector collector = new AuditCollector(maxAuditEvents);
        Map<String, Object> result = mathEvaluator.evaluateAssignments(createAuditedExecutionScope(collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    private static int countMaxAuditEvents(ExecutionPlan plan) {
        int count = 0;
        for (ExecutableAssignment assignment : plan.assignments()) {
            count += switch (assignment) {
                case ExecutableSimpleAssignment s -> 1 + countNodeEvents(s.value());
                case ExecutableDestructuringAssignment d -> d.targets().size() + countNodeEvents(d.value());
            };
        }
        if (plan.resultExpression() != null) {
            count += countNodeEvents(plan.resultExpression());
        }
        return count;
    }

    private static int countNodeEvents(ExecutableNode node) {
        return switch (node) {
            case ExecutableLiteral ignored -> 0;
            case ExecutableDynamicLiteral ignored -> 1;
            case ExecutableIdentifier ignored -> 1;
            case ExecutableFunctionCall f -> {
                int sum = 1;
                for (ExecutableNode arg : f.arguments()) {
                    sum += countNodeEvents(arg);
                }
                yield sum;
            }
            case ExecutableBinaryOp b -> countNodeEvents(b.left()) + countNodeEvents(b.right());
            case ExecutableUnaryOp u -> countNodeEvents(u.operand());
            case ExecutablePostfixOp p -> countNodeEvents(p.operand());
            case ExecutableConditional c -> {
                // Upper bound: count all branches; only one condition+result path runs at runtime.
                int sum = 0;
                for (ExecutableNode cond : c.conditions()) {
                    sum += countNodeEvents(cond);
                }
                for (ExecutableNode res : c.results()) {
                    sum += countNodeEvents(res);
                }
                yield sum + countNodeEvents(c.elseExpression());
            }
            case ExecutableVectorLiteral v -> {
                int sum = 0;
                for (ExecutableNode el : v.elements()) {
                    sum += countNodeEvents(el);
                }
                yield sum;
            }
        };
    }

    public CompiledExpression getCompiledExpression() {
        return compiledExpression;
    }
}
