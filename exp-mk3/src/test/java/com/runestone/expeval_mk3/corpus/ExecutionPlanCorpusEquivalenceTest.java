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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.util.stream.Stream;

/**
 * The ADR 0019 corpus gate for issue #113: every case in {@link ExpressionCorpusExecutionTest}'s
 * planned semantic-and-runtime universe is resolved once and then run through both {@code build} and
 * {@code buildOracle}, asserting they agree in value and in failure. Compile-time diagnostic cases are
 * classified into their semantic or runtime corpus suite but are excluded before JUnit creates these
 * dynamic cases because they do not produce a {@code SemanticModel}. No test is aborted for lacking a
 * plan.
 */
class ExecutionPlanCorpusEquivalenceTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("plannedCases")
    void buildAndBuildOracleAgreeForEveryCorpusCase(String caseId, ExpressionCase expressionCase) {
        ExpressionEnvironment environment = ExpressionCaseEnvironments.environment(expressionCase);
        SemanticModel model = resolve(expressionCase.source(), environment);

        PlanEquivalenceHarness.assertEquivalent(
                model, environment, ExpressionCaseEnvironments.inputs(expressionCase), Clock.systemUTC());
    }

    static Stream<Arguments> plannedCases() {
        return ExpressionCaseLoader.loadAll().stream()
                .filter(expressionCase -> !ExpressionCaseSuite.PARSER.includes(expressionCase))
                .filter(expressionCase -> !(expressionCase.expectedOutcome() instanceof ExpectedDiagnostics))
                .map(expressionCase -> Arguments.of(expressionCase.id(), expressionCase));
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
