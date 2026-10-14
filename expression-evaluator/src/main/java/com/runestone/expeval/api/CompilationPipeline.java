package com.runestone.expeval.api;

import com.runestone.expeval.internal.ast.SemanticAstBuildFailure;
import com.runestone.expeval.internal.ast.SemanticAstBuildResult;
import com.runestone.expeval.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval.internal.ast.SemanticAstBuilder;
import com.runestone.expeval.internal.parser.ExpressionParser;
import com.runestone.expeval.internal.parser.ParseFailure;
import com.runestone.expeval.internal.parser.ParseResult;
import com.runestone.expeval.internal.parser.ParseSuccess;
import com.runestone.expeval.internal.plan.ExecutionPlanBuilder;
import com.runestone.expeval.internal.runtime.RuntimeServices;
import com.runestone.expeval.internal.runtime.ValueShapeValidator;
import com.runestone.expeval.internal.runtime.ConstantShapeException;
import com.runestone.expeval.internal.diagnostics.DiagnosticCode;
import com.runestone.expeval.internal.diagnostics.ExpressionDiagnostics;
import com.runestone.expeval.internal.semantics.SemanticModel;
import com.runestone.expeval.internal.semantics.SemanticResolutionFailure;
import com.runestone.expeval.internal.semantics.SemanticResolutionResult;
import com.runestone.expeval.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval.internal.semantics.SemanticResolver;

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

        if (CompilationSourceLimit.exceeded(source, environment)) {
            return CompilationSourceLimit.failure();
        }
        boolean limited = environment.trustMode() != ExpressionTrustMode.UNSAFE;
        ParseResult parseResult = limited ? PARSER.parse(source, environment.resourceLimits()) : PARSER.parse(source);
        if (parseResult instanceof ParseFailure failure) {
            return new ExpressionCompilationResult.Failure(failure.diagnostics());
        }
        SemanticAstBuildResult astResult = new SemanticAstBuilder().build(
                (ParseSuccess) parseResult, limited ? environment.resourceLimits().maxAstNodeCount() : Integer.MAX_VALUE);
        if (astResult instanceof SemanticAstBuildFailure failure) {
            return new ExpressionCompilationResult.Failure(failure.diagnostics());
        }
        if (limited) {
            for (ExternalSymbol symbol : environment.externalSymbols().values()) {
                ValueShapeValidator.Violation violation = ValueShapeValidator.check(
                        symbol.defaultValue().value(), environment.resourceLimits());
                if (violation != null) {
                    return new ExpressionCompilationResult.Failure(java.util.List.of(ExpressionDiagnostics.create(
                            DiagnosticCode.SEMANTIC_VALUE_SHAPE_EXCEEDED,
                            "Default for '" + symbol.name() + "': " + violation.message(), null)));
                }
            }
        }
        SemanticResolutionResult semanticResult = new SemanticResolver().resolve(
                ((SemanticAstBuildSuccess) astResult).file(), environment);
        if (semanticResult instanceof SemanticResolutionFailure failure) {
            return new ExpressionCompilationResult.Failure(failure.diagnostics());
        }
        SemanticResolutionSuccess success = (SemanticResolutionSuccess) semanticResult;
        SemanticModel model = success.model();
        CompiledExpression compiledExpression;
        try {
            compiledExpression = new CompiledExpression(
                    new ExecutionPlanBuilder().build(model, environment), runtimeServices, success.warnings());
        } catch (ConstantShapeException violation) {
            return new ExpressionCompilationResult.Failure(java.util.List.of(ExpressionDiagnostics.create(
                    DiagnosticCode.SEMANTIC_VALUE_SHAPE_EXCEEDED, violation.getMessage(), violation.span())));
        }
        return new ExpressionCompilationResult.Success(compiledExpression, success.warnings());
    }
}
