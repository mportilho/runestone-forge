package com.runestone.expeval_mk3.api;

import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildFailure;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildResult;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseFailure;
import com.runestone.expeval_mk3.internal.parser.ParseResult;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.plan.ExecutionPlanBuilder;
import com.runestone.expeval_mk3.internal.runtime.RuntimeServices;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionFailure;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionResult;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;

import java.util.Objects;

/**
 * The single, cache-free implementation of {@code source + ExpressionEnvironment + RuntimeServices ->
 * ExpressionCompilationResult}: parsing, Semantic AST construction, semantic resolution, and Plano
 * Imutavel construction. {@link ExpressionEngine}'s cache loader, tests, and JMH all call this same
 * pipeline; no other code path performs a full compilation.
 */
final class CompilationPipeline {

    private static final ExpressionParser PARSER = new ExpressionParser();

    private CompilationPipeline() {
    }

    static ExpressionCompilationResult compile(
            String source, ExpressionEnvironment environment, RuntimeServices runtimeServices) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(environment, "environment");
        Objects.requireNonNull(runtimeServices, "runtimeServices");

        ParseResult parseResult = PARSER.parse(source);
        if (parseResult instanceof ParseFailure failure) {
            return new ExpressionCompilationResult.Failure(failure.diagnostics());
        }
        SemanticAstBuildResult astResult = new SemanticAstBuilder().build((ParseSuccess) parseResult);
        if (astResult instanceof SemanticAstBuildFailure failure) {
            return new ExpressionCompilationResult.Failure(failure.diagnostics());
        }
        SemanticResolutionResult semanticResult = new SemanticResolver().resolve(
                ((SemanticAstBuildSuccess) astResult).file(), environment);
        if (semanticResult instanceof SemanticResolutionFailure failure) {
            return new ExpressionCompilationResult.Failure(failure.diagnostics());
        }
        SemanticResolutionSuccess success = (SemanticResolutionSuccess) semanticResult;
        SemanticModel model = success.model();
        CompiledExpression compiledExpression = new CompiledExpression(
                new ExecutionPlanBuilder().build(model, environment), runtimeServices, success.warnings());
        return new ExpressionCompilationResult.Success(compiledExpression, success.warnings());
    }
}
