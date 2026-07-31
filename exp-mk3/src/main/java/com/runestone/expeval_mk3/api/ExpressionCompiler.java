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
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionFailure;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionResult;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;

import java.util.Objects;

public final class ExpressionCompiler {

    private static final ExpressionParser PARSER = new ExpressionParser();

    private ExpressionCompiler() {
    }

    public static CompiledExpression compile(String source, ExpressionEnvironment environment) {
        return compileOrThrow(source, environment);
    }

    public static CompiledExpression compileOrThrow(String source, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(environment, "environment");

        ParseResult parseResult = PARSER.parse(source);
        if (parseResult instanceof ParseFailure failure) {
            throw new ExpressionCompilationException(failure.diagnostics());
        }
        SemanticAstBuildResult astResult = new SemanticAstBuilder().build((ParseSuccess) parseResult);
        if (astResult instanceof SemanticAstBuildFailure failure) {
            throw new ExpressionCompilationException(failure.diagnostics());
        }
        SemanticResolutionResult semanticResult = new SemanticResolver().resolve(
                ((SemanticAstBuildSuccess) astResult).file(), environment);
        if (semanticResult instanceof SemanticResolutionFailure failure) {
            throw new ExpressionCompilationException(failure.diagnostics());
        }
        SemanticResolutionSuccess success = (SemanticResolutionSuccess) semanticResult;
        return new CompiledExpression(new ExecutionPlanBuilder().build(success.model(), environment));
    }
}
