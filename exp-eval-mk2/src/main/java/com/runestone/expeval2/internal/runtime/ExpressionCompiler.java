package com.runestone.expeval2.internal.runtime;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.environment.ExpressionEnvironmentId;
import com.runestone.expeval2.internal.ast.ExpressionFileNode;
import com.runestone.expeval2.internal.ast.mapping.SemanticAstBuilder;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2ParserFacade;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;

import java.util.Objects;

public final class ExpressionCompiler {

    private final ExpressionEvaluatorV2ParserFacade parserFacade;
    private final SemanticAstBuilder astBuilder;
    private final SemanticResolver semanticResolver;
    private final ExecutionPlanBuilder planBuilder;
    private final Cache<ExpressionCacheKey, CompiledExpression> cache;

    public ExpressionCompiler() {
        this(new ExpressionEvaluatorV2ParserFacade(), new SemanticAstBuilder(), new SemanticResolver(), new ExecutionPlanBuilder());
    }

    ExpressionCompiler(ExpressionEvaluatorV2ParserFacade parserFacade, SemanticAstBuilder astBuilder, SemanticResolver semanticResolver, ExecutionPlanBuilder planBuilder) {
        this.parserFacade = Objects.requireNonNull(parserFacade, "parserFacade must not be null");
        this.astBuilder = Objects.requireNonNull(astBuilder, "astBuilder must not be null");
        this.semanticResolver = Objects.requireNonNull(semanticResolver, "semanticResolver must not be null");
        this.planBuilder = Objects.requireNonNull(planBuilder, "planBuilder must not be null");
        this.cache = Caffeine.newBuilder().maximumSize(1_024).build();
    }

    public CompiledExpression compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        ExpressionCacheKey cacheKey = new ExpressionCacheKey(source, environment.environmentId(), resultType);
        return cache.get(cacheKey, ignored -> compileUncached(source, resultType, environment));
    }

    private CompiledExpression compileUncached(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        ExpressionFileNode ast = switch (resultType) {
            case MATH -> astBuilder.buildMath(parserFacade.parseMath(source).root());
            case LOGICAL -> astBuilder.buildLogical(parserFacade.parseLogical(source).root());
            case ASSIGNMENTS -> astBuilder.buildAssignmentFile(parserFacade.parseAssignments(source).root());
        };
        ResolutionContext resolutionContext = new ResolutionContext(resultType, environment.functionCatalog(), environment.externalSymbolCatalog());
        SemanticModel semanticModel = semanticResolver.resolve(ast, resolutionContext);
        if (semanticModel.hasErrors()) {
            throw new SemanticResolutionException(source, semanticModel.issues());
        }
        ExecutionPlan executionPlan = planBuilder.build(semanticModel);
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
