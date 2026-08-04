package com.runestone.expeval_mk3.internal.plan;

import com.runestone.expeval_mk3.api.ExpressionEnvironment;
import com.runestone.expeval_mk3.api.ExternalSymbolOverwritePolicy;
import com.runestone.expeval_mk3.api.FunctionPurity;
import com.runestone.expeval_mk3.api.ScalarType;
import com.runestone.expeval_mk3.internal.ast.ExpressionFileNode;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuildSuccess;
import com.runestone.expeval_mk3.internal.ast.SemanticAstBuilder;
import com.runestone.expeval_mk3.internal.parser.ExpressionParser;
import com.runestone.expeval_mk3.internal.parser.ParseSuccess;
import com.runestone.expeval_mk3.internal.runtime.BinaryExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.CollectionLiteralExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ConditionalExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.ConstantExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.FunctionCallExecutableNode;
import com.runestone.expeval_mk3.internal.runtime.NullCoalesceExecutableNode;
import com.runestone.expeval_mk3.internal.semantics.SemanticModel;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolutionSuccess;
import com.runestone.expeval_mk3.internal.semantics.SemanticResolver;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the issue #116 lazy/aggregate constant-folding increment: {@code ??} with a constant non-null
 * left operand, the conditional (classic and functional syntax) with a constant condition, an
 * all-constant collection literal, and a call to a function declared foldable with all-constant
 * arguments fold; a construct that does not meet its eligibility does not. Folding {@code ??} and the
 * conditional is a structural rewrite that discards the not-taken branch without ever executing it,
 * which is the asymmetry with eager folding this suite pins down directly.
 */
class LazyConstantFoldingTest {

    @Test
    void foldsNullCoalesceWithAConstantNonNullLeftOperand() {
        SemanticModel model = resolve("5 ?? 10", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("5"));
    }

    @Test
    void doesNotFoldNullCoalesceWithANonConstantLeftOperand() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("x ?? 10", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(NullCoalesceExecutableNode.class);
    }

    @Test
    void nullCoalesceDoesNotEvaluateTheDiscardedOperandWithAnEffect() {
        EffectProbe probe = new EffectProbe();
        ExpressionEnvironment environment = environmentWithProbe(probe);
        SemanticModel model = resolve("5 ?? probe(1)", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC(), probe::reset, probe::orderSnapshot);
        assertThat(probe.orderSnapshot()).isEmpty();
    }

    @Test
    void nullCoalesceDoesNotAnticipateFailureInTheDiscardedOperand() {
        SemanticModel model = resolve("5 ?? (1 / 0)", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("5"));
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void foldsAClassicConditionalWithAConstantCondition() {
        SemanticModel model = resolve("if true then 1 else 2 endif", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("1"));
    }

    @Test
    void foldsAFunctionalConditionalWithAConstantCondition() {
        SemanticModel model = resolve("if(true, 1, false, 2, 3)", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("1"));
    }

    @Test
    void doesNotFoldAConditionalWithANonConstantCondition() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("flag", ScalarType.BOOLEAN, Boolean.TRUE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("if flag then 1 else 2 endif", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(ConditionalExecutableNode.class);
    }

    @Test
    void conditionalDoesNotEvaluateTheNotTakenBranchWithAnEffect() {
        EffectProbe probe = new EffectProbe();
        ExpressionEnvironment environment = environmentWithProbe(probe);
        SemanticModel model = resolve("if true then 1 else probe(2) endif", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of(), Clock.systemUTC(), probe::reset, probe::orderSnapshot);
        assertThat(probe.orderSnapshot()).isEmpty();
    }

    @Test
    void conditionalDoesNotAnticipateFailureInTheNotTakenBranch() {
        SemanticModel model = resolve("if true then 1 else (1 / 0) endif", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((BigDecimal) plan.compute(Map.of(), Clock.systemUTC())).isEqualByComparingTo(new BigDecimal("1"));
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void dropsALeadingConstantFalseBranchButKeepsALaterNonConstantOne() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("flag", ScalarType.BOOLEAN, Boolean.TRUE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("if false then 1 elsif flag then 2 else 3 endif", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(ConditionalExecutableNode.class);
        ConditionalExecutableNode conditional = (ConditionalExecutableNode) plan.resultExpression();
        assertThat(conditional.branches()).hasSize(1);
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of("flag", true), Clock.systemUTC());
        PlanEquivalenceHarness.assertEquivalent(model, environment, Map.of("flag", false), Clock.systemUTC());
    }

    @Test
    void foldsAnAllConstantCollectionLiteral() {
        SemanticModel model = resolve("[1, 2, 3]", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat((List<?>) plan.compute(Map.of(), Clock.systemUTC())).hasSize(3);
    }

    @Test
    void doesNotFoldACollectionLiteralWithANonConstantElement() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel model = resolve("[1, x, 3]", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(CollectionLiteralExecutableNode.class);
    }

    @Test
    void foldsAFoldableFunctionCallWithConstantArgumentsAndEvaluatesItOnlyOnce() {
        CountingProbe probe = new CountingProbe();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(probe, FunctionPurity.FOLDABLE)
                .build();
        SemanticModel model = resolve("identity(7)", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
        assertThat(probe.calls()).isEqualTo(1);
        plan.compute(Map.of(), Clock.systemUTC());
        plan.compute(Map.of(), Clock.systemUTC());
        assertThat(probe.calls()).isEqualTo(1);
    }

    @Test
    void doesNotFoldAFunctionNotDeclaredFoldableEvenWithConstantArguments() {
        CountingProbe probe = new CountingProbe();
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .functionsFrom(probe, FunctionPurity.PURE)
                .build();
        SemanticModel model = resolve("identity(7)", environment);

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment);

        assertThat(plan.resultExpression()).isInstanceOf(FunctionCallExecutableNode.class);
        assertThat(probe.calls()).isZero();
        plan.compute(Map.of(), Clock.systemUTC());
        plan.compute(Map.of(), Clock.systemUTC());
        assertThat(probe.calls()).isEqualTo(2);
    }

    @Test
    void abandonsAFoldableFunctionCallFoldWhenAConstantArgumentFailsToFold() {
        SemanticModel model = resolve("sqrt(2 / 0)", environment());

        ExecutionPlan plan = new ExecutionPlanBuilder().build(model, environment());

        assertThat(plan.resultExpression()).isInstanceOf(FunctionCallExecutableNode.class);
        PlanEquivalenceHarness.assertEquivalent(model, environment(), Map.of(), Clock.systemUTC());
    }

    @Test
    void eagerOperatorCannotDiscardANonConstantOperandButTheLazyConditionalCanDiscardANonConstantBranch() {
        ExpressionEnvironment environment = ExpressionEnvironment.builder()
                .externalSymbol("x", ScalarType.NUMBER, BigDecimal.ONE, ExternalSymbolOverwritePolicy.OVERRIDABLE)
                .build();
        SemanticModel eagerModel = resolve("0 * x", environment);
        SemanticModel lazyModel = resolve("if true then 1 else x endif", environment);

        ExecutionPlan eagerPlan = new ExecutionPlanBuilder().build(eagerModel, environment);
        ExecutionPlan lazyPlan = new ExecutionPlanBuilder().build(lazyModel, environment);

        assertThat(eagerPlan.resultExpression()).isInstanceOf(BinaryExecutableNode.class);
        assertThat(lazyPlan.resultExpression()).isInstanceOf(ConstantExecutableNode.class);
    }

    private static ExpressionEnvironment environment() {
        return ExpressionEnvironment.builder().build();
    }

    private static ExpressionEnvironment environmentWithProbe(EffectProbe probe) {
        return ExpressionEnvironment.builder()
                .functionsFrom(probe, FunctionPurity.IMPURE)
                .build();
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

    /** Records the order impure function calls are observed in, as prior art in {@code PlanOptimizationEquivalenceTest} does. */
    public static final class EffectProbe {
        private final List<BigDecimal> order = new ArrayList<>();

        public BigDecimal probe(BigDecimal tag) {
            order.add(tag);
            return tag;
        }

        void reset() {
            order.clear();
        }

        List<BigDecimal> orderSnapshot() {
            return List.copyOf(order);
        }
    }

    /** Counts how many times its function was actually invoked, at fold time and at execution time alike. */
    public static final class CountingProbe {
        private final AtomicInteger calls = new AtomicInteger();

        public BigDecimal identity(BigDecimal value) {
            calls.incrementAndGet();
            return value;
        }

        int calls() {
            return calls.get();
        }
    }
}
