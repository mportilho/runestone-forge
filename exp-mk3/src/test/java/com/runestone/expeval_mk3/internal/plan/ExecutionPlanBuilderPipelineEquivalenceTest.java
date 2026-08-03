package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionDiagnostic;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves ADR 0019's equivalence contract for issue #113: {@code build} and {@code buildOracle} agree in
 * value and in failure, for the same {@code SemanticModel}, the same environment, and the same inputs.
 * No transformation is installed yet, so the two forms coincide trivially; this is exactly what makes
 * the harness safe to introduce now, ahead of the optimizations it will police. Prior to this ticket this
 * test asserted the two forms produced the same node shape; shape stops being the right thing to assert
 * from the first optimization onward, so it was replaced by value and failure equivalence.
 */
class ExecutionPlanBuilderPipelineEquivalenceTest {

    private static final String SOURCE =
            "a := 1; b := [1, 2, 3]; c := a!; d := (a ?? 2) + c; e := -a; "
                    + "(d > 0 and a between 0 and 10) or (a = 1) or (a ^ 2 = 1) or (sqrt(a) > 0) "
                    + "or (a in b) or (e < 0) or (if(a > 0, true, false))";

    private static final String FAILING_SOURCE = "a := 1 / 0; a";

    @Test
    void buildAndBuildOracleAgreeOnValue() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("dummy", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve(SOURCE, environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

        ExecutionPlan optimized = builder.build(model, environment);
        ExecutionPlan oracle = builder.buildOracle(model, environment);

        assertThat(optimized.hasResult()).isTrue();
        assertThat(optimized.compute(Map.of(), Clock.systemUTC()))
                .isEqualTo(oracle.compute(Map.of(), Clock.systemUTC()));
    }

    @Test
    void buildAndBuildOracleAgreeOnFailureCodeAndSourceSpan() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder().build();
        SemanticModel model = resolve(FAILING_SOURCE, environment);
        ExecutionPlanBuilder builder = new ExecutionPlanBuilder();

        ExecutionPlan optimized = builder.build(model, environment);
        ExecutionPlan oracle = builder.buildOracle(model, environment);

        ExpressionDiagnostic optimizedFailure = diagnosticOf(() -> optimized.compute(Map.of(), Clock.systemUTC()));
        ExpressionDiagnostic oracleFailure = diagnosticOf(() -> oracle.compute(Map.of(), Clock.systemUTC()));

        assertThat(optimizedFailure.code()).isEqualTo(oracleFailure.code());
        assertThat(optimizedFailure.primarySpan()).isEqualTo(oracleFailure.primarySpan());
    }

    private static ExpressionDiagnostic diagnosticOf(Runnable computation) {
        try {
            computation.run();
        } catch (ExpressionExecutionException exception) {
            return exception.diagnostic();
        }
        throw new AssertionError("expected an ExpressionExecutionException, but computation succeeded");
    }

    private static SemanticModel resolve(String source, ExpressionEnvironment environment) {
        ExpressionFileNode ast = ast(source);
        SemanticResolutionSuccess result = (SemanticResolutionSuccess) new SemanticResolver().resolve(ast, environment);
        return result.model();
    }

    private static ExpressionFileNode ast(String source) {
        ParseSuccess parse = (ParseSuccess) new ExpressionParser().parse(source);
        return ((SemanticAstBuildSuccess) new SemanticAstBuilder().build(parse)).file();
    }
}
