package com.runestone.expeval_mk3.api;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves issue #98's evaluation policies through the public compile/compute pipeline: every eager
 * construct evaluates both operands left to right, {@code and}/{@code or}/{@code ??}/conditional/
 * {@code between} evaluate only their contractually required children, and destructuring evaluates
 * its source exactly once. Impure functions record call order so skipped children leave no trace.
 */
class ScalarEvaluationPolicyRuntimeTest {

    @Test
    void andSkipsTheRightOperandWhenTheLeftIsFalse() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("false and truthy(1)", environment(probe));

        assertThat(expression.compute()).isEqualTo(Boolean.FALSE);
        assertThat(probe.order()).isEmpty();
    }

    @Test
    void andSkipsARightOperandThatWouldFail() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("false and boom(1)", environment(probe));

        assertThat(expression.compute()).isEqualTo(Boolean.FALSE);
        assertThat(probe.order()).isEmpty();
    }

    @Test
    void orSkipsTheRightOperandWhenTheLeftIsTrue() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("true or truthy(1)", environment(probe));

        assertThat(expression.compute()).isEqualTo(Boolean.TRUE);
        assertThat(probe.order()).isEmpty();
    }

    @Test
    void orSkipsARightOperandThatWouldFail() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("true or boom(1)", environment(probe));

        assertThat(expression.compute()).isEqualTo(Boolean.TRUE);
        assertThat(probe.order()).isEmpty();
    }

    @Test
    void nandEvaluatesBothOperandsEagerlyUnlikeAnd() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("false nand truthy(1)", environment(probe));

        assertThat(expression.compute()).isEqualTo(Boolean.TRUE);
        assertThat(probe.order()).containsExactly(1);
    }

    @Test
    void norEvaluatesBothOperandsEagerlyUnlikeOr() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("true nor truthy(1)", environment(probe));

        assertThat(expression.compute()).isEqualTo(Boolean.FALSE);
        assertThat(probe.order()).containsExactly(1);
    }

    @Test
    void xorAndXnorEvaluateLeftThenRight() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression xor = ExpressionCompiler.compileOrThrow("truthy(1) xor truthy(2)", environment(probe));
        assertThat(xor.compute()).isEqualTo(Boolean.FALSE);
        assertThat(probe.order()).containsExactly(1, 2);

        EffectProbe xnorProbe = new EffectProbe();
        CompiledExpression xnor = ExpressionCompiler.compileOrThrow("truthy(1) xnor truthy(2)", environment(xnorProbe));
        assertThat(xnor.compute()).isEqualTo(Boolean.TRUE);
        assertThat(xnorProbe.order()).containsExactly(1, 2);
    }

    @Test
    void eagerBinaryStopsEvaluatingOnTheFirstFailure() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("boom(1) nand truthy(2)", environment(probe));

        assertThatThrownBy(expression::compute).isInstanceOf(ExpressionExecutionException.class);
        assertThat(probe.order()).containsExactly(1);
    }

    @Test
    void coalesceStopsAtTheFirstNonNullOperand() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow("track(1) ?? track(2)", environment(probe));

        assertThat((BigDecimal) expression.compute()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(probe.order()).containsExactly(1);
    }

    @Test
    void conditionalEvaluatesConditionsInOrderAndOnlyTheSelectedBranch() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "if falsy(1) then track(10) elsif truthy(2) then track(20) else track(30) endif",
                environment(probe));

        assertThat((BigDecimal) expression.compute()).isEqualByComparingTo(new BigDecimal("20"));
        assertThat(probe.order()).containsExactly(1, 2, 20);
    }

    @Test
    void betweenSkipsTheUpperBoundWhenTheLowerComparisonFails() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "tap(0, 1) between tap(1, 2) and tap(10, 3)", environment(probe));

        assertThat(expression.compute()).isEqualTo(Boolean.FALSE);
        assertThat(probe.order()).containsExactly(1, 2);
    }

    @Test
    void betweenSkipsAnUpperBoundThatWouldFailWhenTheLowerComparisonFails() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "tap(0, 1) between tap(1, 2) and tapBoom(3)", environment(probe));

        assertThat(expression.compute()).isEqualTo(Boolean.FALSE);
        assertThat(probe.order()).containsExactly(1, 2);
    }

    @Test
    void betweenEvaluatesTheUpperBoundWhenTheLowerComparisonPermits() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "tap(5, 1) between tap(1, 2) and tap(10, 3)", environment(probe));

        assertThat(expression.compute()).isEqualTo(Boolean.TRUE);
        assertThat(probe.order()).containsExactly(1, 2, 3);
    }

    @Test
    void collectionLiteralElementsEvaluateLeftToRight() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "[track(1), track(2), track(3)]", environment(probe));

        expression.compute();
        assertThat(probe.order()).containsExactly(1, 2, 3);
    }

    @Test
    void destructuringEvaluatesItsSourceExactlyOnceAndBindsTheTextualOrder() {
        EffectProbe probe = new EffectProbe();
        CompiledExpression expression = ExpressionCompiler.compileOrThrow(
                "[a, b] := source(9); a - b", environment(probe));

        assertThat((BigDecimal) expression.compute()).isEqualByComparingTo(new BigDecimal("-10"));
        assertThat(probe.sourceCallCount()).isEqualTo(1);

        expression.compute();
        assertThat(probe.sourceCallCount()).isEqualTo(2);
    }

    private static ExpressionEnvironment environment(EffectProbe probe) {
        return ExpressionEnvironment.builder().functionsFrom(probe, FunctionPurity.IMPURE).build();
    }

    public static final class EffectProbe {
        private final List<Integer> order = new ArrayList<>();
        private final AtomicInteger sourceCalls = new AtomicInteger();

        public Boolean truthy(BigDecimal tag) {
            order.add(tag.intValue());
            return Boolean.TRUE;
        }

        public Boolean falsy(BigDecimal tag) {
            order.add(tag.intValue());
            return Boolean.FALSE;
        }

        public BigDecimal track(BigDecimal tag) {
            order.add(tag.intValue());
            return tag;
        }

        public Boolean boom(BigDecimal tag) {
            order.add(tag.intValue());
            throw new IllegalStateException("boom:" + tag);
        }

        public BigDecimal tap(BigDecimal value, BigDecimal tag) {
            order.add(tag.intValue());
            return value;
        }

        public BigDecimal tapBoom(BigDecimal tag) {
            order.add(tag.intValue());
            throw new IllegalStateException("boom:" + tag);
        }

        public List<BigDecimal> source(BigDecimal tag) {
            sourceCalls.incrementAndGet();
            return List.of(new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"));
        }

        List<Integer> order() {
            return order;
        }

        int sourceCallCount() {
            return sourceCalls.get();
        }
    }
}
