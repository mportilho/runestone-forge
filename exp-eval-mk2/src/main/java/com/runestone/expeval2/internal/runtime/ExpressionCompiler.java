package com.runestone.expeval2.internal.runtime;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.expeval2.api.CacheConfig;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentId;
import com.runestone.expeval2.internal.ast.ExpressionFileNode;
import com.runestone.expeval2.internal.ast.mapping.SemanticAstBuilder;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2ParserFacade;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;

import java.util.Objects;

/**
 * Compiles expression source strings into {@link CompiledExpression} objects, caching results
 * by {@code (source, environmentId, resultType)} to avoid redundant parse-and-resolve cycles.
 *
 * <h2>Thread safety</h2>
 * <p>This class is thread-safe. The underlying Caffeine cache handles concurrent access without
 * external synchronization.
 *
 * <h2>Cache key</h2>
 * <p>Two compilations share a cache entry when they have the same source text, the same
 * {@link com.runestone.expeval2.environment.ExpressionEnvironmentId}, and the same
 * {@link ExpressionResultType}. The environment ID is derived deterministically from the
 * environment's configuration (providers, external symbols, {@link java.math.MathContext}),
 * so environments built from identical configurations share cache entries automatically.
 *
 * <h2>Lifecycle</h2>
 * <p>Instances can be used as a JVM-wide singleton (via {@link ExpressionRuntimeSupport}) or as
 * scoped, injectable objects when full lifecycle control is required — for example, as a Spring
 * {@code @Bean}. Use {@link #invalidateCache()} to clear all cached entries without discarding
 * the instance.
 */
public final class ExpressionCompiler {

    private final ExpressionEvaluatorV2ParserFacade parserFacade;
    private final SemanticAstBuilder astBuilder;
    private final SemanticResolver semanticResolver;
    private final ExecutionPlanBuilder planBuilder;
    private final Cache<ExpressionCacheKey, CompiledExpression> cache;

    /**
     * Creates a compiler with cache parameters read from JVM system properties, falling back to
     * built-in defaults ({@code maximumSize=1 024}, no TTL) when the properties are absent.
     *
     * <p>System properties consulted:
     * <ul>
     *   <li>{@code expeval.cache.maximumSize} — maximum number of cached entries</li>
     *   <li>{@code expeval.cache.ttlSeconds} — time-to-live in seconds; {@code 0} means no TTL</li>
     * </ul>
     *
     * @see CacheConfig#defaults()
     */
    public ExpressionCompiler() {
        this(new ExpressionEvaluatorV2ParserFacade(), new SemanticAstBuilder(), new SemanticResolver(), new ExecutionPlanBuilder(), CacheConfig.defaults());
    }

    /**
     * Creates a compiler with explicit cache parameters.
     *
     * <p>Use this constructor when injecting the compiler as a managed component and the cache
     * must be configured independently of JVM system properties:
     *
     * <pre>{@code
     * // Spring example
     * @Bean
     * public ExpressionCompiler expressionCompiler() {
     *     return new ExpressionCompiler(new CacheConfig(4_096, Duration.ofHours(1)));
     * }
     * }</pre>
     *
     * @param cacheConfig cache settings; must not be {@code null}
     */
    public ExpressionCompiler(CacheConfig cacheConfig) {
        this(new ExpressionEvaluatorV2ParserFacade(), new SemanticAstBuilder(), new SemanticResolver(), new ExecutionPlanBuilder(), cacheConfig);
    }

    ExpressionCompiler(ExpressionEvaluatorV2ParserFacade parserFacade, SemanticAstBuilder astBuilder,
                       SemanticResolver semanticResolver, ExecutionPlanBuilder planBuilder, CacheConfig cacheConfig) {
        this.parserFacade = Objects.requireNonNull(parserFacade, "parserFacade must not be null");
        this.astBuilder = Objects.requireNonNull(astBuilder, "astBuilder must not be null");
        this.semanticResolver = Objects.requireNonNull(semanticResolver, "semanticResolver must not be null");
        this.planBuilder = Objects.requireNonNull(planBuilder, "planBuilder must not be null");
        Objects.requireNonNull(cacheConfig, "cacheConfig must not be null");
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().maximumSize(cacheConfig.maximumSize());
        if (cacheConfig.expireAfterWrite() != null) {
            caffeine.expireAfterWrite(cacheConfig.expireAfterWrite());
        }
        this.cache = caffeine.build();
    }

    /**
     * Compiles {@code source} into a {@link CompiledExpression}, returning a cached result when
     * an equivalent compilation has already been performed under the same environment and result type.
     *
     * @param source      expression source text; must not be blank
     * @param resultType  expected result kind (math, logical, or assignments); must not be {@code null}
     * @param environment execution environment providing the function catalog and external symbols;
     *                    must not be {@code null}
     * @return a compiled, reusable expression
     * @throws IllegalArgumentException    if {@code source} is blank
     * @throws NullPointerException        if any argument is {@code null}
     * @throws SemanticResolutionException if the expression references unknown symbols or functions
     */
    public CompiledExpression compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        ExpressionCacheKey cacheKey = new ExpressionCacheKey(source, environment.environmentId(), resultType);
        return cache.get(cacheKey, ignored -> compileUncached(source, resultType, environment));
    }

    /**
     * Removes all entries from the compilation cache.
     *
     * <p>Call this after a function-catalog or external-symbol update that cannot be reflected
     * through a new {@link com.runestone.expeval2.environment.ExpressionEnvironmentId}. Compilations
     * already in progress complete normally; only subsequent cache lookups are affected.
     *
     * <p>When operating through the JVM-wide singleton, prefer
     * {@link ExpressionRuntimeSupport#invalidateCache()} instead of calling this method directly.
     */
    public void invalidateCache() {
        cache.invalidateAll();
    }

    private CompiledExpression compileUncached(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        ExpressionFileNode ast = switch (resultType) {
            case MATH -> astBuilder.buildMath(parserFacade.parseMath(source).root());
            case LOGICAL -> astBuilder.buildLogical(parserFacade.parseLogical(source).root());
            case ASSIGNMENTS -> astBuilder.buildAssignmentFile(parserFacade.parseAssignments(source).root());
        };
        ResolutionContext resolutionContext = new ResolutionContext(resultType, environment.functionCatalog(), environment.externalSymbolCatalog(), environment.typeHintCatalog());
        SemanticModel semanticModel = semanticResolver.resolve(ast, resolutionContext);
        if (semanticModel.hasErrors()) {
            throw new SemanticResolutionException(source, semanticModel.issues());
        }
        ExecutionPlan executionPlan = planBuilder.build(
                semanticModel,
                environment.runtimeServices(),
                environment.externalSymbolCatalog(),
                environment.typeHintCatalog(),
                environment.mathContext()
        );
        return new CompiledExpression(source, resultType, semanticModel, executionPlan);
    }

    private record ExpressionCacheKey(
            String source,
            ExpressionEnvironmentId environmentId,
            ExpressionResultType resultType
    ) {

        public ExpressionCacheKey {
            if (source == null || source.isBlank()) {
                throw new IllegalArgumentException("source must not be blank");
            }
            Objects.requireNonNull(environmentId, "environmentId must not be null");
            Objects.requireNonNull(resultType, "resultType must not be null");
        }
    }
}
