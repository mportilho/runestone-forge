package com.runestone.expeval.internal.runtime;

import com.runestone.expeval.api.CompilationIssue;
import com.runestone.expeval.api.CompilationPosition;
import com.runestone.expeval.api.SemanticResolutionException;
import com.runestone.expeval.environment.ExpressionEnvironment;
import com.runestone.expeval.internal.ast.ExpressionFileNode;
import com.runestone.expeval.internal.ast.SourceSpan;
import com.runestone.expeval.internal.ast.mapping.SemanticAstBuilder;
import com.runestone.expeval.internal.grammar.ExpressionEvaluatorParserFacade;
import com.runestone.expeval.internal.grammar.ExpressionResultType;

import java.util.List;
import java.util.Objects;

/**
 * Internal compiler implementation for expression source strings.
 *
 * <p>Application code should use {@link com.runestone.expeval.api.ExpressionEngine}. Cache
 * ownership belongs to the engine runtime; this class only runs the parse, semantic resolution,
 * and execution-plan pipeline.
 *
 * <h2>Thread safety</h2>
 * <p>This class is thread-safe because it holds only immutable pipeline collaborators.
 */
final class ExpressionCompiler {

    private final ExpressionEvaluatorParserFacade parserFacade;
    private final SemanticAstBuilder astBuilder;
    private final SemanticResolver semanticResolver;
    private final ExecutionPlanBuilder planBuilder;

    ExpressionCompiler() {
        this(new ExpressionEvaluatorParserFacade(), new SemanticAstBuilder(), new SemanticResolver(), new ExecutionPlanBuilder());
    }

    ExpressionCompiler(ExpressionEvaluatorParserFacade parserFacade, SemanticAstBuilder astBuilder,
                       SemanticResolver semanticResolver, ExecutionPlanBuilder planBuilder) {
        this.parserFacade = Objects.requireNonNull(parserFacade, "parserFacade must not be null");
        this.astBuilder = Objects.requireNonNull(astBuilder, "astBuilder must not be null");
        this.semanticResolver = Objects.requireNonNull(semanticResolver, "semanticResolver must not be null");
        this.planBuilder = Objects.requireNonNull(planBuilder, "planBuilder must not be null");
    }

    /**
     * Compiles {@code source} into a {@link CompiledExpression}.
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
        return compileRuntime(source, resultType, environment).getCompiledExpression();
    }

    public ExpressionRuntimeSupport compileRuntime(String source, ExpressionResultType resultType, ExpressionEnvironment environment) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source must not be blank");
        }
        Objects.requireNonNull(resultType, "resultType must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        RuntimeServices runtimeServices = new RuntimeServices(environment.getDataConversionService());
        ExpressionFileNode ast = switch (resultType) {
            case MATH -> astBuilder.buildMath(parserFacade.parseMath(source).root());
            case LOGICAL -> astBuilder.buildLogical(parserFacade.parseLogical(source).root());
            case ASSIGNMENTS -> astBuilder.buildAssignmentFile(parserFacade.parseAssignments(source).root());
        };
        ResolutionContext resolutionContext = new ResolutionContext(resultType, environment.functionCatalog(), environment.externalSymbolCatalog(), environment.typeHintCatalog());
        SemanticModel semanticModel = semanticResolver.resolve(ast, resolutionContext);
        if (semanticModel.hasErrors()) {
            List<CompilationIssue> issues = semanticModel.issues().stream()
                    .map(issue -> {
                        SourceSpan span = issue.sourceSpan();
                        CompilationPosition position = new CompilationPosition(span.startLine(), span.startColumn(), span.endColumn());
                        return new CompilationIssue(issue.code(), issue.message(), position);
                    })
                    .toList();
            throw new SemanticResolutionException(source, issues);
        }
        ExecutionPlan executionPlan = planBuilder.build(
                semanticModel,
                runtimeServices,
                environment.externalSymbolCatalog(),
                environment.typeHintCatalog(),
                environment.mathContext()
        );
        CompiledExpression compiledExpression = new CompiledExpression(source, resultType, semanticModel, executionPlan);
        return ExpressionRuntimeSupport.from(compiledExpression, runtimeServices, environment.mathContext());
    }

}
