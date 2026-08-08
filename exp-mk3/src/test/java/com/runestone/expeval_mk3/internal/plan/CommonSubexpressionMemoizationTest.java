package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExpressionExecutionException;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.NodeId;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.MemoizedExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the issue #121 contract for Subexpressao Comum Memoizada: lazy in-place memoization on plan
 * frame slots appended past the semantic {@code frameSize}, with no hoisting, no cross-element sharing
 * for a subtree that reads Item Atual, and no cross-reassignment sharing for a subtree that reads an
 * internal symbol. {@link PlanEquivalenceHarness} carries the value/failure/order equivalence proof;
 * this file adds the three traps the Etapa 7 plan calls out explicitly plus the internal frame-slot
 * assertion that has no public consumer before Etapa 10.
 */
class CommonSubexpressionMemoizationTest {

    @Test
    void memoizesARepeatedPureCallOverAnOverridableExternalAndInvokesItOnce() {
        AtomicInteger invocations = new AtomicInteger();
        ExpressionEnvironment environment = environmentWithCountingIdentity(invocations);
        SemanticModel model = resolve("count(x) + count(x)", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);
        Object result = plan.compute(Map.of("x", new BigDecimal("5")), Clock.systemUTC());

        assertThat(result).isEqualTo(new BigDecimal("10"));
        assertThat(invocations).hasValue(1);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of("x", new BigDecimal("5")), Clock.systemUTC());
    }

    @Test
    void neverEvaluatesAMemoizedOccurrenceInsideANotTakenLazyBranch() {
        AtomicInteger invocations = new AtomicInteger();
        ExpressionEnvironment environment = environmentWithCountingIdentity(invocations);
        SemanticModel model = resolve("if(flag; count(x); 0) + count(x)", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);
        Map<String, Object> inputs = Map.of("flag", false, "x", new BigDecimal("7"));
        Object result = plan.compute(inputs, Clock.systemUTC());

        assertThat(result).isEqualTo(new BigDecimal("7"));
        assertThat(invocations).hasValue(1);
        PlanEquivalenceHarness.assertEquivalent(model, environment, inputs, Clock.systemUTC());
    }

    @Test
    void recomputesACurrentItemDependentSubtreeForEveryElementInsteadOfCachingTheFirst() {
        SemanticModel model = resolve("items := [1, 2, 3]; items.map(@ -> @ + @)", environmentWithoutSymbols());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environmentWithoutSymbols());
        Object result = plan.compute(Map.of(), Clock.systemUTC());

        assertThat((List<?>) result).extracting(value -> ((BigDecimal) value).intValue())
                .containsExactly(2, 4, 6);
        PlanEquivalenceHarness.assertEquivalent(model, environmentWithoutSymbols(), Map.of(), Clock.systemUTC());
    }

    @Test
    void neverSharesAMemoSlotAcrossAReassignmentOfAnInternalSymbol() {
        SemanticModel model = resolve(
                "x := 1; y := x + 1; x := 2; z := x + 1; y + z", environmentWithoutSymbols());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environmentWithoutSymbols());
        Object result = plan.compute(Map.of(), Clock.systemUTC());

        assertThat(result).isEqualTo(new BigDecimal("5"));
        PlanEquivalenceHarness.assertEquivalent(model, environmentWithoutSymbols(), Map.of(), Clock.systemUTC());
    }

    @Test
    void failsAtTheFirstExecutedOccurrenceWithTheSameCodeAndSpanAsTheOracle() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("(10 / x) + (10 / x)", environment);

        ExecutionPlan optimized = new ExecutionPlanBuilder().build(model, environment);

        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of("x", BigDecimal.ZERO), Clock.systemUTC());
        assertThatThrownBy(() -> optimized.compute(Map.of("x", BigDecimal.ZERO), Clock.systemUTC()))
                .isInstanceOf(ExpressionExecutionException.class);
    }

    @Test
    void appendsMemoSlotsPastTheSemanticFrameSizeForAnEligibleRepeatedSubtree() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("(x + 1) * (x + 1)", environment);
        int semanticFrameSize = model.frameLayout().frameSize();

        CommonSubexpressionAnalysis analysis = CommonSubexpressionAnalyzer.analyze(model);

        assertThat(analysis.memoSlotCount()).isGreaterThan(0);
        assertThat(analysis.memoSlotsByNodeId()).isNotEmpty();
        for (Map.Entry<NodeId, Integer> entry : analysis.memoSlotsByNodeId().entrySet()) {
            assertThat(entry.getValue()).isGreaterThanOrEqualTo(semanticFrameSize);
        }
    }

    @Test
    void doesNotRecordAnyMemoSlotInOracleMode() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("(x + 1) * (x + 1)", environment);

        ExecutionPlan oracle = OraclePlanFixtures.buildOracle(model, environment);

        assertThat(oracle.resultExpression()).isNotInstanceOf(MemoizedExecutableNode.class);
    }

    private static ExpressionEnvironment environmentWithCountingIdentity(AtomicInteger invocations) {
        return ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ZERO, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .externalSymbol("flag", ScalarType.BOOLEAN, true, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .functionsFrom(new CountingIdentity(invocations), FunctionPurity.PURE)
                .build();
    }

    private static ExpressionEnvironment environmentWithoutSymbols() {
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

    /**
     * Declared {@code PURE} so it is CSE-eligible; the invocation counter is an out-of-band test probe,
     * not a language-observable effect, the same trick {@code EffectProbe} classes elsewhere use to make
     * memoization/folding decisions observable from outside the expression itself.
     */
    public static final class CountingIdentity {
        private final AtomicInteger invocations;

        CountingIdentity(AtomicInteger invocations) {
            this.invocations = invocations;
        }

        public BigDecimal count(BigDecimal value) {
            invocations.incrementAndGet();
            return value;
        }
    }
}
