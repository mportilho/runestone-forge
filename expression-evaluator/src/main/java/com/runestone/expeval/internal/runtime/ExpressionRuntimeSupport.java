package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.*;
import com.runestone.expeval.catalog.ExternalSymbolCatalog;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.internal.grammar.ParsingException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bridge between the compilation pipeline and the public expression API.
 *
 * <p>This class serves two roles:
 * <ol>
 *   <li><strong>Static factory</strong> — the {@code compileMath}, {@code compileLogical}, and
 *       {@code compileAssignments} methods compile a source string and return a ready-to-evaluate
 *       instance backed by the JVM-wide {@link ExpressionCompiler} singleton.</li>
 *   <li><strong>Evaluation context</strong> — each instance holds the compiled plan and the
 *       evaluators for a single expression. Call {@code computeMath} / {@code computeLogical} /
 *       {@code computeAssignments} with a {@code Map<String, Object>} of variable values to
 *       evaluate. The same instance may safely be called concurrently from multiple threads
 *       because there is no mutable per-instance state; each call receives its own
 *       {@link ExecutionScope}.</li>
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
 * <p>All static factory methods and all instance {@code compute*} methods are thread-safe.
 * Compiled instances hold no mutable state: variable values are supplied per call via a
 * {@code Map<String, Object>} and each call builds its own {@link ExecutionScope}.
 */
public final class ExpressionRuntimeSupport {

    private static volatile ExpressionCompiler COMPILER;

    /**
     * Configures the JVM-wide expression compiler <strong>before the first compilation</strong>.
     *
     * <p>If the singleton has already been initialized the call is silently ignored.
     * Use {@link #reconfigure(CacheConfig)} when you need to replace an already-initialized compiler.
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
     * singleton has been initialized.
     */
    public static void reconfigure(CacheConfig cacheConfig) {
        Objects.requireNonNull(cacheConfig, "cacheConfig must not be null");
        synchronized (ExpressionRuntimeSupport.class) {
            COMPILER = new ExpressionCompiler(cacheConfig);
        }
    }

    /**
     * Removes all entries from the JVM-wide compiler cache without replacing the compiler instance.
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
    /**
     * Immutable snapshot of default values seeded from the {@link ExternalSymbolCatalog} at
     * compile time. Shared across all {@code compute*} calls; never mutated after construction.
     */
    private final Object[] defaultValues;
    /**
     * Precomputed binding metadata for external symbols referenced by this expression.
     */
    private final Map<String, ExternalBindingPlan> externalBindingsByName;
    private final Set<String> internalSymbolNames;
    private final RuntimeServices runtimeServices;
    private final Evaluator<BigDecimal> mathEvaluator;
    private final Evaluator<Boolean> logicalEvaluator;
    private final boolean hasAssignments;
    private final int internalSymbolCount;
    private final int externalSymbolCount;
    private final int maxAuditEvents;

    private ExpressionRuntimeSupport(CompiledExpression compiledExpression,
                                     Object[] defaultValues,
                                     Map<String, ExternalBindingPlan> externalBindingsByName,
                                     Set<String> internalSymbolNames,
                                     RuntimeServices runtimeServices,
                                     MathContext mathContext) {
        this.compiledExpression = Objects.requireNonNull(compiledExpression, "compiledExpression must not be null");
        this.defaultValues = Objects.requireNonNull(defaultValues, "defaultValues must not be null");
        this.externalBindingsByName = Objects.requireNonNull(externalBindingsByName, "externalBindingsByName must not be null");
        this.internalSymbolNames = Objects.requireNonNull(internalSymbolNames, "internalSymbolNames must not be null");
        this.runtimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices must not be null");
        Objects.requireNonNull(mathContext, "mathContext must not be null");
        this.mathEvaluator = new MathEvaluator(compiledExpression, runtimeServices, mathContext);
        this.logicalEvaluator = new LogicalEvaluator(compiledExpression, runtimeServices, mathContext);
        this.hasAssignments = !compiledExpression.executionPlan().assignments().isEmpty();
        this.internalSymbolCount = compiledExpression.semanticModel().internalSymbolsByName().size();
        this.externalSymbolCount = compiledExpression.executionPlan().externalSymbolsCount();
        this.maxAuditEvents = compiledExpression.executionPlan().maxAuditEvents();
    }

    // -------------------------------------------------------------------------
    // Static factory methods
    // -------------------------------------------------------------------------

    /**
     * Compiles a math expression using the JVM-wide singleton compiler.
     */
    public static ExpressionRuntimeSupport compileMath(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.MATH, environment);
    }

    /**
     * Compiles a math expression using an explicit {@link ExpressionCompiler}.
     */
    public static ExpressionRuntimeSupport compileMath(String source, ExpressionEnvironment environment, ExpressionCompiler compiler) {
        return compile(source, ExpressionResultType.MATH, environment, compiler);
    }

    /**
     * Compiles a logical expression using the JVM-wide singleton compiler.
     */
    public static ExpressionRuntimeSupport compileLogical(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.LOGICAL, environment);
    }

    /**
     * Compiles a logical expression using an explicit {@link ExpressionCompiler}.
     */
    public static ExpressionRuntimeSupport compileLogical(String source, ExpressionEnvironment environment, ExpressionCompiler compiler) {
        return compile(source, ExpressionResultType.LOGICAL, environment, compiler);
    }

    /**
     * Compiles an assignment block using the JVM-wide singleton compiler.
     */
    public static ExpressionRuntimeSupport compileAssignments(String source, ExpressionEnvironment environment) {
        return compile(source, ExpressionResultType.ASSIGNMENTS, environment);
    }

    /**
     * Compiles an assignment block using an explicit {@link ExpressionCompiler}.
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
            ExpressionRuntimeSupport runtime = compile(source, resultType, environment);
            SemanticModel model = runtime.getCompiledExpression().semanticModel();
            Set<String> assignedVariables = model.internalSymbolsByName().keySet();
            Set<String> userVariables = model.externalSymbolsByName().keySet();
            Set<String> functions = model.functionBindings().values().stream()
                    .map(b -> b.functionRef().name())
                    .collect(Collectors.toSet());
            return ValidationResult.ok(source, assignedVariables, userVariables, functions);
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
     */
    public static ExpressionRuntimeSupport compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        return compile(source, resultType, environment, getCompiler());
    }

    /**
     * Compiles an expression using an explicit {@link ExpressionCompiler}.
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
        RuntimeServices runtimeServices = environment.runtimeServices();
        SemanticModel semanticModel = compiledExpression.semanticModel();
        Object[] defaults = compiledExpression.executionPlan().defaults();
        Map<String, ExternalBindingPlan> bindings = compiledExpression.executionPlan().externalBindings();
        return new ExpressionRuntimeSupport(compiledExpression, defaults, bindings, semanticModel.internalSymbolsByName().keySet(), runtimeServices,
                environment.mathContext());
    }


    /**
     * Builds the per-call overrides array, leaving immutable defaults shared across evaluations.
     */
    private Object[] buildOverrides(Map<String, Object> userValues) {
        if (userValues == null || userValues.isEmpty()) {
            return null;
        }
        Object[] result = new Object[externalSymbolCount];
        Arrays.fill(result, ExecutionScope.UNBOUND);
        for (Map.Entry<String, Object> entry : userValues.entrySet()) {
            String name = entry.getKey();
            ExternalBindingPlan binding = lookupExternalBinding(name);
            if (!binding.overridable()) {
                throw new IllegalStateException("symbol '" + name + "' is not overridable");
            }
            result[binding.index()] = runtimeServices.coerceToResolvedType(
                    entry.getValue(),
                    binding.declaredType());
        }
        return result;
    }

    private ExternalBindingPlan lookupExternalBinding(String name) {
        ExternalBindingPlan binding = externalBindingsByName.get(name);
        if (binding != null) {
            return binding;
        }
        if (internalSymbolNames.contains(name)) {
            throw new IllegalArgumentException("symbol '" + name + "' is internal to the expression");
        }
        throw new IllegalArgumentException("unknown external symbol '" + name + "'");
    }

    private ExecutionScope createExecutionScope(Map<String, Object> userValues) {
        if (userValues == null || userValues.isEmpty()) {
            if (hasAssignments) {
                return ExecutionScope.from(defaultValues, internalSymbolCount);
            }
            return ExecutionScope.readOnly(defaultValues);
        }
        Object[] overrides = buildOverrides(userValues);
        if (hasAssignments) {
            if (defaultValues.length == 0) {
                return ExecutionScope.from(overrides, internalSymbolCount);
            }
            return ExecutionScope.from(overrides, defaultValues, internalSymbolCount);
        }
        if (defaultValues.length == 0) {
            return ExecutionScope.readOnly(overrides);
        }
        return ExecutionScope.readOnly(overrides, defaultValues);
    }

    private ExecutionScope createAuditedExecutionScope(Map<String, Object> userValues, AuditCollector collector) {
        if (userValues == null || userValues.isEmpty()) {
            if (hasAssignments) {
                return ExecutionScope.fromWithAudit(defaultValues, internalSymbolCount, collector);
            }
            return ExecutionScope.readOnlyWithAudit(defaultValues, collector);
        }
        Object[] overrides = buildOverrides(userValues);
        if (hasAssignments) {
            if (defaultValues.length == 0) {
                return ExecutionScope.fromWithAudit(overrides, internalSymbolCount, collector);
            }
            return ExecutionScope.fromWithAudit(overrides, defaultValues, internalSymbolCount, collector);
        }
        if (defaultValues.length == 0) {
            return ExecutionScope.readOnlyWithAudit(overrides, collector);
        }
        return ExecutionScope.readOnlyWithAudit(overrides, defaultValues, collector);
    }

    // -------------------------------------------------------------------------
    // Evaluation
    // -------------------------------------------------------------------------

    public BigDecimal computeMath(Map<String, Object> values) {
        return mathEvaluator.evaluate(createExecutionScope(values));
    }

    public boolean computeLogical(Map<String, Object> values) {
        return logicalEvaluator.evaluate(createExecutionScope(values));
    }

    public AuditResult<BigDecimal> computeMathWithAudit(Map<String, Object> values) {
        AuditCollector collector = new AuditCollector(maxAuditEvents);
        BigDecimal result = mathEvaluator.evaluate(createAuditedExecutionScope(values, collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public AuditResult<Boolean> computeLogicalWithAudit(Map<String, Object> values) {
        AuditCollector collector = new AuditCollector(maxAuditEvents);
        boolean result = logicalEvaluator.evaluate(createAuditedExecutionScope(values, collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public Map<String, Object> computeAssignments(Map<String, Object> values) {
        return mathEvaluator.evaluateAssignments(createExecutionScope(values));
    }

    public AuditResult<Map<String, Object>> computeAssignmentsWithAudit(Map<String, Object> values) {
        AuditCollector collector = new AuditCollector(maxAuditEvents);
        Map<String, Object> result = mathEvaluator.evaluateAssignments(createAuditedExecutionScope(values, collector));
        return new AuditResult<>(result, collector.buildTrace());
    }

    public CompiledExpression getCompiledExpression() {
        return compiledExpression;
    }

}
