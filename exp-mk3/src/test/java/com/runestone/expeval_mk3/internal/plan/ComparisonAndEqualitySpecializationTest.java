package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.ComparableComparisonExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.EqualsEqualityExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.NumberComparisonExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.NumberEqualityExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the issue #126 construction-time node choice for order comparison and equality: the resolved
 * operand type picks {@link NumberComparisonExecutableNode}/{@link ComparableComparisonExecutableNode}
 * or {@link NumberEqualityExecutableNode}/{@link EqualsEqualityExecutableNode} once, at plan-build time,
 * instead of {@code ExpressionRuntime.compareValues}/{@code structuralEquals} re-dispatching by type on
 * every evaluation. Which concrete node class was built is never asserted (the issue's own testing
 * decision, matching {@code MembershipDownloadFoldingTest}'s precedent): {@link PlanEquivalenceHarness}
 * and the computed result are the proof.
 */
class ComparisonAndEqualitySpecializationTest {

    @Test
    void numberComparisonSpecializesAndAgreesWithTheOracleOnEveryOperator() {
        SemanticModel model = resolve(
                "(5 > 3) and (3 >= 3) and !(3 > 5) and (3 <= 3) and (2 < 3) and !(3 < 2)", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void stringComparisonSpecializesAndAgreesWithTheOracle() {
        SemanticModel model = resolve("(\"b\" > \"a\") and !(\"a\" > \"b\") and (\"a\" <= \"a\")", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    /**
     * The rule most likely to break silently under a specialized fast path (issue #126's own risk
     * note): {@code NUMBER} equality must keep comparing by {@code compareTo}, never {@code equals},
     * so a scale difference alone never makes two numerically equal operands compare unequal.
     */
    @Test
    void numberEqualityAcrossDifferentScalesStaysTrueAndAgreesWithTheOracle() {
        SemanticModel model = resolve("(1.0 = 1) and !(1.00 <> 1)", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void stringEqualitySpecializesAndAgreesWithTheOracle() {
        SemanticModel model = resolve("(\"a\" = \"a\") and !(\"a\" = \"b\") and (\"a\" <> \"b\")", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    /**
     * A {@code COLLECTION} operand type declines specialization (issue #126): the generic node's
     * recursive structural comparison keeps comparing contained {@code NUMBER} elements by
     * {@code compareTo}, never {@code List.equals}, so a per-element scale difference alone never
     * makes two structurally equal collections compare unequal.
     */
    @Test
    void collectionEqualityKeepsStructuralComparisonAndAgreesWithTheOracle() {
        SemanticModel model = resolve("[1.0, 2.0] = [1, 2]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    /**
     * A {@code MAP} operand type declines specialization the same way {@code COLLECTION} does (issue
     * #126): the generic node's recursive structural comparison keeps comparing contained
     * {@code NUMBER} values by {@code compareTo}, never {@code Map.equals}.
     */
    @Test
    void mapEqualityKeepsStructuralComparisonAndAgreesWithTheOracle() {
        Map<String, BigDecimal> left = new LinkedHashMap<>();
        left.put("k1", BigDecimal.valueOf(1));
        left.put("k2", new BigDecimal("2.00"));
        Map<String, BigDecimal> right = new LinkedHashMap<>();
        right.put("k1", new BigDecimal("1.0"));
        right.put("k2", BigDecimal.valueOf(2));
        ExpressionEnvironment mapEnvironment = ExpressionEnvironment.builder()
                .externalSymbol("left", new MapType(ScalarType.NUMBER), left, ExternalSymbolOverwritePolicy.FIXED)
                .externalSymbol("right", new MapType(ScalarType.NUMBER), right, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("left = right", mapEnvironment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, mapEnvironment);

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, mapEnvironment, Map.of(), Clock.systemUTC());
    }

    private static ExpressionEnvironment environment() {
        return ExpressionEnvironment.builder().build();
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
