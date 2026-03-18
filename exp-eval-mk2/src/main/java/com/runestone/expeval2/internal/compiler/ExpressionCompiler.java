package com.runestone.expeval2.internal.compiler;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.runestone.expeval2.internal.ast.ExpressionFileNode;
import com.runestone.expeval2.internal.ast.mapping.SemanticAstBuilder;
import com.runestone.expeval2.environment.ExpressionEnvironment;
import com.runestone.expeval2.internal.grammar.ExpressionEvaluatorV2ParserFacade;
import com.runestone.expeval2.internal.grammar.ExpressionResultType;
import com.runestone.expeval2.internal.runtime.ResolutionContext;
import com.runestone.expeval2.internal.runtime.SemanticModel;
import com.runestone.expeval2.internal.runtime.SemanticResolutionException;
import com.runestone.expeval2.internal.runtime.SemanticResolver;

import java.util.Objects;

public final class ExpressionCompiler {

    private final ExpressionEvaluatorV2ParserFacade parserFacade;
    private final SemanticAstBuilder astBuilder;
    private final SemanticResolver semanticResolver;
    private final Cache<ExpressionCacheKey, CompiledExpression> cache;

    public ExpressionCompiler() {
        this(new ExpressionEvaluatorV2ParserFacade(), new SemanticAstBuilder(), new SemanticResolver());
    }

    ExpressionCompiler(ExpressionEvaluatorV2ParserFacade parserFacade, SemanticAstBuilder astBuilder, SemanticResolver semanticResolver) {
        this.parserFacade = Objects.requireNonNull(parserFacade, "parserFacade must not be null");
        this.astBuilder = Objects.requireNonNull(astBuilder, "astBuilder must not be null");
        this.semanticResolver = Objects.requireNonNull(semanticResolver, "semanticResolver must not be null");
        this.cache = Caffeine.newBuilder().maximumSize(1_024).build();
    }

    public CompiledExpression compile(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        ExpressionCacheKey cacheKey = new ExpressionCacheKey(source, environment.environmentId(), resultType);
        return cache.get(cacheKey, ignored -> compileUncached(source, resultType, environment));
    }

    private CompiledExpression compileUncached(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(resultType, "resultType must not be null");
        ExpressionFileNode ast = switch (resultType) {
            case MATH -> astBuilder.buildMath(parserFacade.parseMath(source).root());
            case LOGICAL -> astBuilder.buildLogical(parserFacade.parseLogical(source).root());
        };
        ResolutionContext resolutionContext = new ResolutionContext(resultType, environment.functionCatalog(), environment.externalSymbolCatalog());
        SemanticModel semanticModel = semanticResolver.resolve(ast, resolutionContext);
        if (semanticModel.hasErrors()) {
            throw new SemanticResolutionException(source, semanticModel.issues());
        }
        return new CompiledExpression(source, resultType, semanticModel);
    }
}
