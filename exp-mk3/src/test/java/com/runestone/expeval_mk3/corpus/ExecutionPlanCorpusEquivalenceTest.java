package com.runestone.expeval_mk3.corpus;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.plan.PlanEquivalenceHarness;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;

/**
 * The ADR 0019 corpus gate for issue #113: every case in {@link ExpressionCorpusExecutionTest}'s
 * semantic-and-runtime universe is resolved once and then run through both {@code build} and
 * {@code buildOracle}, asserting they agree in value and in failure. That universe already excludes the
 * parser and migration phases, which never reach a successful {@code SemanticModel} and so have nothing
 * for a plan builder to build; within it, this suite adds no further exclusion by phase or tag. No
 * transformation is installed yet, so this suite is born green; it is the regression net every later
 * optimization increment in Etapa 7 must keep green.
 *
 * <p>A case whose expected outcome is a compile-time {@link ExpectedDiagnostic} likewise produces no
 * {@code SemanticModel} to build a plan from, whether that diagnostic surfaces during semantic
 * resolution or, as for a statically-detectable safe-navigation failure filed under the runtime phase,
 * during a later stage of the same compile step; such a case is recorded as aborted rather than silently
 * passed, exactly as it is out of scope for {@link ExpressionCorpusExecutionTest}'s own assertion against
 * the public compiler.
 */
class ExecutionPlanCorpusEquivalenceTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("com.runestone.expeval_mk3.corpus.ExpressionCorpusExecutionTest#semanticAndRuntimeCases")
    void buildAndBuildOracleAgreeForEveryCorpusCase(String caseId, ExpressionCase expressionCase) {
        Assumptions.assumeTrue(
                !(expressionCase.expectedOutcome() instanceof ExpectedDiagnostic),
                "compile-time diagnostic case: no SemanticModel to build a plan from");

        ExpressionEnvironment environment = ExpressionCaseEnvironments.environment(expressionCase);
        SemanticModel model = resolve(expressionCase.source(), environment);

        PlanEquivalenceHarness.assertEquivalent(
                model, environment, ExpressionCaseEnvironments.inputs(expressionCase), Clock.systemUTC());
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
