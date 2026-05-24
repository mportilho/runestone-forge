package com.runestone.expeval.api;

import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.environment.ExpressionEnvironmentBuilder;
import com.runestone.expeval.internal.grammar.ExpressionResultType;
import com.runestone.expeval.internal.grammar.ParsingException;
import com.runestone.expeval.internal.runtime.CompiledExpression;
import com.runestone.expeval.internal.runtime.ExpressionCompilationCache;
import com.runestone.expeval.internal.runtime.ExpressionRuntimeSupport;
import com.runestone.expeval.internal.runtime.SemanticModel;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User-facing entry point for compiling and validating expressions with an isolated runtime
 * configuration.
 *
 * <p>An engine owns its compilation cache. Create a dedicated instance when different application
 * modules, tenants, or tests need independent cache size, TTL, or lifecycle control. Compiled
 * expressions produced by the same engine share cache entries by {@code (source, environmentId,
 * resultType)}.
 *
 * <pre>{@code
 * ExpressionEngine engine = ExpressionEngine.builder()
 *         .cacheConfig(new CacheConfig(4_096, Duration.ofHours(1)))
 *         .build();
 *
 * MathExpression expression = engine.compileMath("a + b", environment);
 * }</pre>
 *
 * <h2>Thread safety</h2>
 * <p>Instances are thread-safe. The underlying cache supports concurrent access and compiled
 * expressions hold no mutable evaluation state.
 */
public final class ExpressionEngine {

    private static volatile ExpressionEngine DEFAULT_ENGINE;

    private final ExpressionCompilationCache compilationCache;

    /**
     * Creates an engine with cache settings read from system-property defaults.
     *
     * @see CacheConfig#defaults()
     */
    public ExpressionEngine() {
        this(CacheConfig.defaults());
    }

    /**
     * Creates an engine with explicit cache settings.
     *
     * @param cacheConfig cache settings; must not be {@code null}
     */
    public ExpressionEngine(CacheConfig cacheConfig) {
        this.compilationCache = new ExpressionCompilationCache(Objects.requireNonNull(cacheConfig, "cacheConfig must not be null"));
    }

    private ExpressionEngine(Builder builder) {
        this(builder.cacheConfig);
    }

    /**
     * Returns a builder for engine runtime configuration.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Configures the JVM-wide default engine before the first static compilation.
     *
     * <p>If the default engine has already been initialized, this method is a no-op. Use
     * {@link #reconfigureDefault(CacheConfig)} when replacement is required.
     */
    public static void configureDefault(CacheConfig cacheConfig) {
        Objects.requireNonNull(cacheConfig, "cacheConfig must not be null");
        if (DEFAULT_ENGINE == null) {
            synchronized (ExpressionEngine.class) {
                if (DEFAULT_ENGINE == null) {
                    DEFAULT_ENGINE = new ExpressionEngine(cacheConfig);
                }
            }
        }
    }

    /**
     * Replaces the JVM-wide default engine immediately.
     */
    public static void reconfigureDefault(CacheConfig cacheConfig) {
        Objects.requireNonNull(cacheConfig, "cacheConfig must not be null");
        synchronized (ExpressionEngine.class) {
            DEFAULT_ENGINE = new ExpressionEngine(cacheConfig);
        }
    }

    /**
     * Removes all compiled expressions from the JVM-wide default engine cache.
     */
    public static void invalidateDefaultCache() {
        defaultEngine().invalidateCache();
    }

    static ExpressionEngine defaultEngine() {
        ExpressionEngine engine = DEFAULT_ENGINE;
        if (engine == null) {
            synchronized (ExpressionEngine.class) {
                engine = DEFAULT_ENGINE;
                if (engine == null) {
                    DEFAULT_ENGINE = engine = new ExpressionEngine(CacheConfig.defaults());
                }
            }
        }
        return engine;
    }

    /**
     * Compiles a math expression with an empty environment.
     */
    public MathExpression compileMath(String source) {
        return compileMath(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Compiles a math expression with the given environment.
     */
    public MathExpression compileMath(String source, ExpressionEnvironment environment) {
        return MathExpression.from(compileRuntime(source, ExpressionResultType.MATH, environment));
    }

    /**
     * Compiles a logical expression with an empty environment.
     */
    public LogicalExpression compileLogical(String source) {
        return compileLogical(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Compiles a logical expression with the given environment.
     */
    public LogicalExpression compileLogical(String source, ExpressionEnvironment environment) {
        return LogicalExpression.from(compileRuntime(source, ExpressionResultType.LOGICAL, environment));
    }

    /**
     * Compiles an assignment block with an empty environment.
     */
    public AssignmentExpression compileAssignments(String source) {
        return compileAssignments(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Compiles an assignment block with the given environment.
     */
    public AssignmentExpression compileAssignments(String source, ExpressionEnvironment environment) {
        return AssignmentExpression.from(compileRuntime(source, ExpressionResultType.ASSIGNMENTS, environment));
    }

    /**
     * Validates a math expression with an empty environment.
     */
    public ValidationResult validateMath(String source) {
        return validateMath(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Validates a math expression with the given environment.
     */
    public ValidationResult validateMath(String source, ExpressionEnvironment environment) {
        return validate(source, ExpressionResultType.MATH, environment);
    }

    /**
     * Validates a logical expression with an empty environment.
     */
    public ValidationResult validateLogical(String source) {
        return validateLogical(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Validates a logical expression with the given environment.
     */
    public ValidationResult validateLogical(String source, ExpressionEnvironment environment) {
        return validate(source, ExpressionResultType.LOGICAL, environment);
    }

    /**
     * Validates an assignment block with an empty environment.
     */
    public ValidationResult validateAssignments(String source) {
        return validateAssignments(source, ExpressionEnvironmentBuilder.empty());
    }

    /**
     * Validates an assignment block with the given environment.
     */
    public ValidationResult validateAssignments(String source, ExpressionEnvironment environment) {
        return validate(source, ExpressionResultType.ASSIGNMENTS, environment);
    }

    /**
     * Removes all compiled expressions from this engine's cache.
     */
    public void invalidateCache() {
        compilationCache.invalidateCache();
    }

    private ExpressionRuntimeSupport compileRuntime(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        try {
            CompiledExpression compiled = compilationCache.compile(source, resultType, environment);
            return ExpressionRuntimeSupport.from(compiled, environment);
        } catch (SemanticResolutionException e) {
            throw new ExpressionCompilationException(source, e.issues(), e);
        }
    }

    private ValidationResult validate(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        try {
            ExpressionRuntimeSupport runtime = compileRuntime(source, resultType, environment);
            SemanticModel model = runtime.getCompiledExpression().semanticModel();
            Set<String> assignedVariables = model.internalSymbolsByName().keySet();
            Set<String> userVariables = model.externalSymbolsByName().keySet();
            Set<String> functions = model.functionBindings().values().stream()
                    .map(binding -> binding.functionRef().name())
                    .collect(Collectors.toSet());
            return ValidationResult.ok(source, assignedVariables, userVariables, functions);
        } catch (ExpressionCompilationException e) {
            return ValidationResult.failed(source, e.issues());
        } catch (ParsingException e) {
            List<CompilationIssue> issues = e.errors().stream()
                    .map(error -> new CompilationIssue(
                            IssueCode.SYNTAX_ERROR,
                            error.message(),
                            new CompilationPosition(error.line(), error.charPositionInLine(), error.charPositionInLine() + 1)
                    ))
                    .toList();
            return ValidationResult.failed(
                    source,
                    issues.isEmpty() ? List.of(new CompilationIssue(IssueCode.SYNTAX_ERROR, "syntax error")) : issues
            );
        }
    }

    /**
     * Builder for {@link ExpressionEngine} runtime configuration.
     */
    public static final class Builder {

        private CacheConfig cacheConfig = CacheConfig.defaults();

        private Builder() {
        }

        /**
         * Sets cache settings for the engine being built.
         */
        public Builder cacheConfig(CacheConfig cacheConfig) {
            this.cacheConfig = Objects.requireNonNull(cacheConfig, "cacheConfig must not be null");
            return this;
        }

        /**
         * Creates the configured engine.
         */
        public ExpressionEngine build() {
            return new ExpressionEngine(this);
        }
    }
}
