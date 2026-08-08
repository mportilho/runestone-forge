package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;

import java.util.Objects;

/**
 * Test-only seam letting benchmarks outside this package (issue #120) reach {@link ExecutionPlanBuilder}'s
 * package-private {@code buildOracle}, the same Unoptimized Oracle that {@link PlanEquivalenceHarness}
 * validates against, without exposing the oracle path as a public library seam.
 */
public final class OraclePlanFixtures {

    private OraclePlanFixtures() {
    }

    public static SemanticModel resolve(String source, ExpressionEnvironment environment) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(environment, "environment");
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        ExpressionFileNode ast = ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
        SemanticResolutionSuccess result = (SemanticResolutionSuccess) new SemanticResolver().resolve(ast, environment);
        return result.model();
    }

    public static ExecutionPlan buildOptimized(SemanticModel model, ExpressionEnvironment environment) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(environment, "environment");
        return new ExecutionPlanBuilder().build(model, environment);
    }

    public static ExecutionPlan buildOracle(SemanticModel model, ExpressionEnvironment environment) {
        Objects.requireNonNull(model, "model");
        Objects.requireNonNull(environment, "environment");
        return new ExecutionPlanBuilder().buildOracle(model, environment);
    }

    public static ExecutionPlan compileOptimized(String source, ExpressionEnvironment environment) {
        return buildOptimized(resolve(source, environment), environment);
    }

    public static ExecutionPlan compileOracle(String source, ExpressionEnvironment environment) {
        return buildOracle(resolve(source, environment), environment);
    }
}
