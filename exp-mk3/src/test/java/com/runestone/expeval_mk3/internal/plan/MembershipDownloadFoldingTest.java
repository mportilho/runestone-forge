package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.CollectionType;
import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.MapType;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.ConstantExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.MembershipExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the issue #119 membership download: a constant right-hand collection at or above the
 * eight-element threshold downloads to a lookup structure chosen by element type, a collection or map
 * element never downloads, a constant map right-hand side keeps its {@code containsKey} lookup, and a
 * non-constant right-hand side is left exactly as it was. Which lookup structure a downloaded case picked
 * is never asserted directly (issue #119's testing decision): {@link PlanEquivalenceHarness} and the
 * computed result are the proof, exactly as the corpus already characterizes scale, threshold, element
 * type, and map right-hand side (issue #114).
 */
class MembershipDownloadFoldingTest {

    @Test
    void preEvaluatesAConstantCollectionBelowTheThresholdAndKeepsTheLinearScan() {
        SemanticModel model = resolve("5 in [1, 2, 3, 4, 5]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(MembershipExecutableNode.class);
        MembershipExecutableNode membership = (MembershipExecutableNode) plan.resultExpression();
        assertThat(membership.collection()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void doesNotFoldMembershipWithANonConstantRightSide() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol(
                        "items", new CollectionType(ScalarType.NUMBER),
                        List.of(BigDecimal.ONE, BigDecimal.TWO), ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("1 in items", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(MembershipExecutableNode.class);
        MembershipExecutableNode membership = (MembershipExecutableNode) plan.resultExpression();
        assertThat(membership.collection()).isNotInstanceOf(ConstantExecutableNode.class);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
    }

    @Test
    void downloadsANumberElementCollectionAboveTheThresholdAndComputesTheSameResult() {
        SemanticModel model = resolve("9 in [1, 2, 3, 4, 5, 6, 7, 8, 9]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void downloadsTheNegatedFormOfANumberElementCollectionAboveTheThreshold() {
        SemanticModel model = resolve("100 not in [1, 2, 3, 4, 5, 6, 7, 8, 9]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void downloadsAStringElementCollectionAboveTheThresholdAndComputesTheSameResult() {
        SemanticModel model = resolve(
                "\"i\" in [\"a\", \"b\", \"c\", \"d\", \"e\", \"f\", \"g\", \"h\", \"i\"]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void downloadsTheNegatedFormOfAStringElementCollectionAboveTheThreshold() {
        SemanticModel model = resolve(
                "\"z\" not in [\"a\", \"b\", \"c\", \"d\", \"e\", \"f\", \"g\", \"h\", \"i\"]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void keepsScaleMismatchTrueAboveTheThresholdBecauseTheDownloadNeverUsesAHashSetOfNumber() {
        SemanticModel model = resolve("1.0 in [1, 2, 3, 4, 5, 6, 7, 8]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void doesNotDownloadACollectionElementAboveTheThresholdButStillPreEvaluatesIt() {
        SemanticModel model = resolve(
                "[1, 2] in [[1, 2], [3, 4], [5, 6], [7, 8], [9, 10], [11, 12], [13, 14], [15, 16], [17, 18]]",
                environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(MembershipExecutableNode.class);
        MembershipExecutableNode membership = (MembershipExecutableNode) plan.resultExpression();
        assertThat(membership.collection()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    /**
     * A behavioral, non-structural proof of the same rule: a {@code HashSet} of collection elements
     * would compare contained numbers by {@code equals} through {@code List.equals}, so {@code [1.0]}
     * would be a different set member than {@code [1]}; the structural scan this collection element
     * never downloads away from still finds it, exactly because {@code structuralEquals} compares
     * {@code NUMBER} by {@code compareTo}.
     */
    @Test
    void keepsScaleMismatchTrueForACollectionElementAboveTheThresholdBecauseItNeverDownloads() {
        SemanticModel model = resolve(
                "[1.0] in [[1], [2], [3], [4], [5], [6], [7], [8], [9]]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void keepsKeyLookupForAConstantMapRightSideAboveTheThresholdAndOnlyPreEvaluatesIt() {
        Map<String, BigDecimal> unordered = new LinkedHashMap<>();
        for (int index = 1; index <= 9; index++) {
            unordered.put("k" + index, BigDecimal.valueOf(index));
        }
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("scores", new MapType(ScalarType.NUMBER), unordered, ExternalSymbolOverwritePolicy.FIXED)
                .build();
        SemanticModel model = resolve("\"k9\" in scores", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(MembershipExecutableNode.class);
        MembershipExecutableNode membership = (MembershipExecutableNode) plan.resultExpression();
        assertThat(membership.collection()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(membership.collectionType()).isInstanceOf(MapType.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC());
    }

    @Test
    void computesTheSameResultForARepeatedElementAboveTheThreshold() {
        SemanticModel model = resolve("5 in [1, 2, 3, 4, 5, 5, 6, 7, 8]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void preEvaluatesAConstantSingleElementCollectionAndComputesTheSameResult() {
        SemanticModel model = resolve("5 in [5]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(MembershipExecutableNode.class);
        MembershipExecutableNode membership = (MembershipExecutableNode) plan.resultExpression();
        assertThat(membership.collection()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(plan.compute(Map.of(), Clock.systemUTC())).isEqualTo(true);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
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
